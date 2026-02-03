const authService = require('../services/authService');

const signup = async (req, res) => {
  try {
    const user = await authService.signup(req.body);
    res.status(201).json({ 
      status: 'SUCCESS',
      message: 'Verification code dispatched.', 
      email: user.email 
    });
  } catch (error) {
    console.error('SIGNUP ERROR:', error.message);
    res.status(400).json({ 
      status: 'ERROR',
      message: error.message 
    });
  }
};

const verifyOtp = async (req, res) => {
  try {
    const { email, otp } = req.body;
    await authService.verifyOtp(email, otp);
    res.json({ status: 'SUCCESS', message: 'Identity verified.' });
  } catch (error) {
    console.error('VERIFY OTP ERROR:', error.message);
    res.status(400).json({ 
      status: 'ERROR',
      message: error.message 
    });
  }
};

const login = async (req, res) => {
  try {
    const { email, password } = req.body;
    const { user, token } = await authService.login(email, password);
    res.json({ status: 'SUCCESS', token, user: { id: user.id, name: user.name, email: user.email } });
  } catch (error) {
    console.error('LOGIN ERROR:', error.message);
    res.status(401).json({ 
      status: 'ERROR',
      message: error.message 
    });
  }
};

const forgotPassword = async (req, res) => {
  try {
    const { email } = req.body;
    await authService.generateResetToken(email);
    res.json({ status: 'SUCCESS', message: 'Recovery code dispatched.' });
  } catch (error) {
    res.status(404).json({ status: 'ERROR', message: error.message });
  }
};

const resetPassword = async (req, res) => {
  try {
    const { email, otp, newPassword } = req.body;
    await authService.resetPassword(email, otp, newPassword);
    res.json({ status: 'SUCCESS', message: 'Password updated successfully.' });
  } catch (error) {
    res.status(400).json({ status: 'ERROR', message: error.message });
  }
};

module.exports = { signup, verifyOtp, login, forgotPassword, resetPassword };
