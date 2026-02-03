const nodemailer = require('nodemailer');
require('dotenv').config();

const transporter = nodemailer.createTransport({
  host: "smtp-relay.brevo.com",
  port: 587,
  secure: false, // STARTTLS
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS,
  },
  tls: {
    rejectUnauthorized: false
  }
});

const sendOTP = async (email, otp) => {
  const sender = process.env.SENDER_EMAIL || process.env.EMAIL_USER;
  
  const mailOptions = {
    from: {
        name: "APPLE AI Assistant",
        address: sender
    },
    to: email,
    subject: `${otp} is your verification code`,
    html: `
      <div style="font-family: sans-serif; max-width: 400px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
        <h2 style="color: #333; text-align: center;">Security Verification</h2>
        <p style="font-size: 16px; color: #555;">Use the code below to access your account:</p>
        <div style="background: #f4f4f4; padding: 15px; text-align: center; font-size: 36px; font-weight: bold; letter-spacing: 5px; color: #007bff; margin: 20px 0;">
          ${otp}
        </div>
        <p style="color: #888; font-size: 12px; text-align: center;">Code expires in 10 minutes.</p>
      </div>
    `,
  };

  try {
    // CRITICAL: We MUST await here on Vercel so the process isn't killed
    const info = await transporter.sendMail(mailOptions);
    console.log('--- MAIL SENT SUCCESSFULLY ---', info.messageId);
    return info;
  } catch (error) {
    console.error('--- MAIL FAILED ---', error.message);
    throw error;
  }
};

module.exports = { sendOTP };
