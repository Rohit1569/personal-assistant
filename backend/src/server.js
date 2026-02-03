require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const sequelize = require('./config/database');

const app = express();

// VERSION 1.0.1 - FORCE REBUILD
console.log('>>> APPLE AI CORE STARTING - VERSION 1.0.1');

app.use(helmet({ contentSecurityPolicy: false }));
app.use(cors({ origin: '*', credentials: true }));
app.use(express.json());

// Import Routes
const authRoutes = require('./routes/authRoutes');
const usageRoutes = require('./routes/usageRoutes');

// 1. Diagnostics
app.get('/api/health', (req, res) => res.json({ status: 'ONLINE', version: '1.0.1' }));

// 2. Database Handshake
app.use(async (req, res, next) => {
  try {
    if (req.path === '/api/health' || req.path === '/') return next();
    const { sequelize } = require('./config/database');
    await sequelize.authenticate();
    next();
  } catch (err) {
    console.error('DATABASE AUTH ERROR:', err.message);
    res.status(503).json({ error: "DATABASE_OFFLINE" });
  }
});

app.use('/api/auth', authRoutes);
app.use('/api/usage', usageRoutes);

app.get('/', (req, res) => res.send('APPLE AI CORE ACTIVE V1.0.1'));

if (process.env.NODE_ENV !== 'production') {
  const PORT = process.env.PORT || 5002;
  app.listen(PORT, '0.0.0.0', () => console.log(`Server at ${PORT}`));
}

module.exports = app;
