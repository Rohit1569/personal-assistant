const usageService = require('../services/usageService');

const getStats = async (req, res) => {
  try {
    const stats = await usageService.getStats(req.user.id);
    res.json(stats);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

const incrementStat = async (req, res) => {
  try {
    const { feature } = req.body;
    if (!feature) return res.status(400).json({ error: 'Feature name is required' });
    
    const updatedStats = await usageService.incrementStat(req.user.id, feature);
    res.json(updatedStats);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
};

module.exports = { getStats, incrementStat };
