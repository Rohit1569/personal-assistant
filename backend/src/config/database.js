require('dotenv').config();
const { Sequelize } = require('sequelize');
const pg = require('pg');

// USE DATABASE_URL FROM ENVIRONMENT
const dbUrl = process.env.DATABASE_URL;

if (!dbUrl) {
  console.error('--- CRITICAL ERROR: DATABASE_URL not found in environment ---');
  process.exit(1);
}

const sequelize = new Sequelize(dbUrl, {
  dialect: 'postgres',
  dialectModule: pg,
  logging: false,
  dialectOptions: {
    ssl: {
      require: true,
      rejectUnauthorized: false
    }
  },
  define: {
    timestamps: false,
    underscored: true
  }
});

module.exports = { sequelize };
