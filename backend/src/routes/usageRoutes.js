const express = require('express');
const router = express.Router();
const usageController = require('../controllers/usageController');
const authenticate = require('../middleware/authMiddleware');

router.get('/me', authenticate, usageController.getStats);
router.post('/increment', authenticate, usageController.incrementStat);

module.exports = router;
