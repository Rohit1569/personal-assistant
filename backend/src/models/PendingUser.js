const db = require('../config/database');
const bcrypt = require('bcryptjs');
const { DataTypes } = require('sequelize');

const PendingUser = db.sequelize.define('PendingUser', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true
  },
  name: {
    type: DataTypes.STRING,
    allowNull: false
  },
  email: {
    type: DataTypes.STRING,
    unique: true,
    allowNull: false
  },
  password: {
    type: DataTypes.STRING,
    allowNull: false
  },
  otp_code: {
    type: DataTypes.STRING,
    allowNull: false
  },
  expires_at: {
    type: DataTypes.DATE,
    allowNull: false
  },
  created_at: {
    type: DataTypes.DATE,
    allowNull: false,
    defaultValue: new Date(),
    field: 'created_at'
  },
  updated_at: {
    type: DataTypes.DATE,
    allowNull: false,
    defaultValue: new Date(),
    field: 'updated_at'
  }
}, {
  tableName: 'PendingUsers',
  timestamps: false, // FORCE-STOP AUTOMATIC TIMESTAMPS
  hooks: {
    beforeCreate: async (pendingUser) => {
      if (pendingUser.password) {
        pendingUser.password = await bcrypt.hash(pendingUser.password, 10);
      }
    }
  }
});

module.exports = PendingUser;
