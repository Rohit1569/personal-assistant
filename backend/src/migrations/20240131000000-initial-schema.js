'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    // 1. Create Users Table
    await queryInterface.createTable('Users', {
      id: { type: Sequelize.UUID, defaultValue: Sequelize.UUIDV4, primaryKey: true, allowNull: false },
      name: { type: Sequelize.STRING, allowNull: false },
      email: { type: Sequelize.STRING, unique: true, allowNull: false },
      password_hash: { type: Sequelize.STRING, allowNull: false },
      is_verified: { type: Sequelize.BOOLEAN, defaultValue: false },
      created_at: { allowNull: false, type: Sequelize.DATE },
      updated_at: { allowNull: false, type: Sequelize.DATE }
    });

    // 2. Create PendingUsers Table
    await queryInterface.createTable('PendingUsers', {
      id: { type: Sequelize.UUID, defaultValue: Sequelize.UUIDV4, primaryKey: true, allowNull: false },
      name: { type: Sequelize.STRING, allowNull: false },
      email: { type: Sequelize.STRING, unique: true, allowNull: false },
      password: { type: Sequelize.STRING, allowNull: false },
      otp_code: { type: Sequelize.STRING, allowNull: false },
      expires_at: { type: Sequelize.DATE, allowNull: false },
      created_at: { allowNull: false, type: Sequelize.DATE },
      updated_at: { allowNull: false, type: Sequelize.DATE }
    });

    // 3. Create UsageStats Table
    await queryInterface.createTable('UsageStats', {
      id: { type: Sequelize.UUID, defaultValue: Sequelize.UUIDV4, primaryKey: true, allowNull: false },
      user_id: {
        type: Sequelize.UUID,
        allowNull: false,
        references: { model: 'Users', key: 'id' },
        onUpdate: 'CASCADE',
        onDelete: 'CASCADE'
      },
      messages_sent_count: { type: Sequelize.INTEGER, defaultValue: 0 },
      meetings_scheduled_count: { type: Sequelize.INTEGER, defaultValue: 0 },
      emails_sent_count: { type: Sequelize.INTEGER, defaultValue: 0 },
      cab_booking_count: { type: Sequelize.INTEGER, defaultValue: 0 },
      other_feature_usage_count: { type: Sequelize.INTEGER, defaultValue: 0 },
      created_at: { allowNull: false, type: Sequelize.DATE },
      updated_at: { allowNull: false, type: Sequelize.DATE }
    });

    // 4. Create Otps Table
    await queryInterface.createTable('Otps', {
      id: { allowNull: false, autoIncrement: true, primaryKey: true, type: Sequelize.INTEGER },
      email: { type: Sequelize.STRING, allowNull: false },
      code: { type: Sequelize.STRING, allowNull: false },
      expires_at: { type: Sequelize.DATE, allowNull: false },
      created_at: { allowNull: false, type: Sequelize.DATE },
      updated_at: { allowNull: false, type: Sequelize.DATE }
    });

    // 5. Create PasswordResetTokens Table
    await queryInterface.createTable('PasswordResetTokens', {
      id: { type: Sequelize.UUID, defaultValue: Sequelize.UUIDV4, primaryKey: true, allowNull: false },
      user_id: {
        type: Sequelize.UUID,
        allowNull: false,
        references: { model: 'Users', key: 'id' },
        onUpdate: 'CASCADE',
        onDelete: 'CASCADE'
      },
      token: { type: Sequelize.STRING, allowNull: false },
      expires_at: { type: Sequelize.DATE, allowNull: false },
      created_at: { allowNull: false, type: Sequelize.DATE },
      updated_at: { allowNull: false, type: Sequelize.DATE }
    });
  },

  down: async (queryInterface, Sequelize) => {
    // Adding CASCADE: true forces the drop even if other tables depend on them
    await queryInterface.dropTable('PasswordResetTokens', { cascade: true });
    await queryInterface.dropTable('Otps', { cascade: true });
    await queryInterface.dropTable('UsageStats', { cascade: true });
    await queryInterface.dropTable('PendingUsers', { cascade: true });
    await queryInterface.dropTable('Users', { cascade: true });
  }
};
