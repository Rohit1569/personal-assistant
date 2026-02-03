const { sequelize } = require('./src/config/database');
const User = require('./src/models/User');

async function debug() {
  console.log('--- STARTING SQL DIAGNOSTIC ---');
  try {
    await sequelize.authenticate();
    console.log('1. Database Connection: OK');
    
    console.log('2. Generating SQL for User.findOne()...');
    // This will force Sequelize to generate the query and we can see the columns
    await User.findOne({ 
      logging: (sql) => {
        console.log('\n>>> GENERATED SQL:');
        console.log(sql);
        console.log('\n');
      }
    });
    
    console.log('3. Test Result: SUCCESS (No naming errors)');
  } catch (err) {
    console.error('\n!!! DIAGNOSTIC FAILED !!!');
    console.error('Error Message:', err.message);
    if (err.parent) {
      console.error('Postgres Detail:', err.parent.detail);
    }
  } finally {
    await sequelize.close();
    process.exit();
  }
}

debug();
