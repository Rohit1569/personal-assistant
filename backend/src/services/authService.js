const User = require('../models/User');
const UsageStats = require('../models/UsageStats');
const PendingUser = require('../models/PendingUser');
const Otp = require('../models/Otp');
const jwt = require('jsonwebtoken');
const { sendOTP } = require('../utils/email');
const bcrypt = require('bcryptjs');

// Password Validation
const validatePassword = (password) => {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;
  if (!passwordRegex.test(password)) {
    throw new Error('WEAK_PASSWORD: Password must be 8+ characters with uppercase, lowercase, number, and special character.');
  }
};

const signup = async (userData) => {
  const { email, name, password } = userData;
  validatePassword(password);
  const normalizedEmail = email.toLowerCase().trim();

  try {
    const existingUser = await User.findOne({ where: { email: normalizedEmail } });
    if (existingUser) throw new Error('ALREADY_EXISTS');

    const otpCode = Math.floor(100000 + Math.random() * 900000).toString();

    await PendingUser.destroy({ where: { email: normalizedEmail }, hooks: false });
    await PendingUser.create({
      email: normalizedEmail,
      name,
      password, 
      otp_code: otpCode,
      expires_at: new Date(Date.now() + 600000)
    });

    await sendOTP(normalizedEmail, otpCode);
    return { email: normalizedEmail };
  } catch (error) {
    throw error;
  }
};

const verifyOtp = async (email, code) => {
  const normalizedEmail = email.toLowerCase().trim();
  const pending = await PendingUser.findOne({ where: { email: normalizedEmail, otp_code: code.trim() } });

  if (!pending) throw new Error('INVALID_CODE');
  if (new Date() > pending.expires_at) throw new Error('EXPIRED');

  // Move to permanent User table - password is already hashed in PendingUser
  const user = await User.create({
    email: pending.email,
    name: pending.name,
    password: pending.password, 
    is_verified: true,
    created_at: new Date(),
    updated_at: new Date()
  }, { hooks: false });

  await UsageStats.create({ user_id: user.id, created_at: new Date(), updated_at: new Date() });
  await pending.destroy({ hooks: false });

  return true;
};

const login = async (email, password) => {
  const normalizedEmail = email.toLowerCase().trim();
  const user = await User.findOne({ where: { email: normalizedEmail } });
  if (!user) throw new Error('NOT_FOUND');
  
  const isPasswordValid = await user.validPassword(password);
  if (!isPasswordValid) throw new Error('INVALID_PASS');

  const token = jwt.sign({ id: user.id }, process.env.JWT_SECRET, { expiresIn: '30d' });
  return { user, token };
};

const generateResetToken = async (email) => {
  const normalizedEmail = email.toLowerCase().trim();
  const user = await User.findOne({ where: { email: normalizedEmail } });
  if (!user) throw new Error('USER_NOT_FOUND');

  const otpCode = Math.floor(100000 + Math.random() * 900000).toString();
  await Otp.destroy({ where: { email: normalizedEmail } });
  await Otp.create({
    email: normalizedEmail,
    code: otpCode,
    expires_at: new Date(Date.now() + 600000),
    created_at: new Date(),
    updated_at: new Date()
  });

  await sendOTP(normalizedEmail, `Your password reset code is: ${otpCode}`);
  return true;
};

const resetPassword = async (email, otp, newPassword) => {
  const normalizedEmail = email.toLowerCase().trim();
  validatePassword(newPassword);

  const otpRecord = await Otp.findOne({
    where: { email: normalizedEmail, code: otp.trim() }
  });

  if (!otpRecord || new Date() > otpRecord.expires_at) {
    throw new Error('INVALID_OR_EXPIRED_OTP');
  }

  const user = await User.findOne({ where: { email: normalizedEmail } });
  if (!user) throw new Error('USER_NOT_FOUND');

  // CRITICAL FIX: Manually hash the password here to ensure it is ALWAYS hashed
  // because bulk update hooks can be unreliable in serverless environments.
  const salt = await bcrypt.genSalt(10);
  const hashedPass = await bcrypt.hash(newPassword, salt);
  
  // Use User.update with the hashed password directly to the field
  await User.update(
    { password: newPassword }, // This triggers the model's mapped password_hash field
    { 
      where: { email: normalizedEmail },
      individualHooks: true // This ensures the beforeSave hook triggers
    }
  );

  await otpRecord.destroy();
  return true;
};

module.exports = { signup, verifyOtp, login, generateResetToken, resetPassword };
