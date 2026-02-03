const PendingUser = require('./src/models/PendingUser');
const { sequelize } = require('./src/config/database');

async function test() {
  try {
    console.log('Testing PendingUser.create...');
    await PendingUser.create({
      name: 'Test',
      email: 'test' + Date.now() + '@example.com',
      password: 'password',
      otp_code: '123456',
      expires_at: new Date(Date.now() + 600000),
      created_at: new Date(),
      updated_at: new Date()
    }, {
      logging: console.log
    });
    console.log('SUCCESS');
  } catch (err) {
    console.error('FAILED:', err.message);
    if (err.parent) console.error('PG ERROR:', err.parent.sql);
  } finally {
    await sequelize.close();
  }
}

test();
