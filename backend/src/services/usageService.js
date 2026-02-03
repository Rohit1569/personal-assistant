const UsageStats = require('../models/UsageStats');

const getStats = async (userId) => {
  return await UsageStats.findOne({ where: { user_id: userId } });
};

const incrementStat = async (userId, feature) => {
  const stats = await UsageStats.findOne({ where: { user_id: userId } });
  if (!stats) throw new Error('Stats not found');

  switch (feature.toUpperCase()) {
    case 'MESSAGE':
      stats.messages_sent_count += 1;
      break;
    case 'MEETING':
      stats.meetings_scheduled_count += 1;
      break;
    case 'EMAIL':
      stats.emails_sent_count += 1;
      break;
    case 'CAB':
      stats.cab_booking_count += 1;
      break;
    default:
      stats.other_feature_usage_count += 1;
  }

  await stats.save();
  return stats;
};

module.exports = { getStats, incrementStat };
