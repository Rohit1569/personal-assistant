node -e "const P = require('./src/models/PendingUser'); P.findOne().then(() => console.log('Signup Test: SUCCESS')).catch(e => console.error('FAIL:', e.message))"
    