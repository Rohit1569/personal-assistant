const User = require('./src/models/User');
const { sequelize } = require('./src/config/database');

async function test() {
  try {
    console.log('Testing User.findOne...');
    await User.findOne({
      where: { email: 'test@example.com' },
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
