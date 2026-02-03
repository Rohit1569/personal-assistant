const db = require('../config/database');
const { DataTypes } = require('sequelize');

const UsageStats = db.sequelize.define('UsageStats', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true
  },
  user_id: {
    type: DataTypes.UUID,
    allowNull: false
  },
  messages_sent_count: { type: DataTypes.INTEGER, defaultValue: 0 },
  meetings_scheduled_count: { type: DataTypes.INTEGER, defaultValue: 0 },
  emails_sent_count: { type: DataTypes.INTEGER, defaultValue: 0 },
  cab_booking_count: { type: DataTypes.INTEGER, defaultValue: 0 },
  other_feature_usage_count: { type: DataTypes.INTEGER, defaultValue: 0 },
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
  tableName: 'UsageStats',
  timestamps: false // PERMANENTLY DISABLE THE AUTOMATIC ENGINE
});

module.exports = UsageStats;
