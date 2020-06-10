module.exports = {
  extends: ['@commitlint/config-angular'],
};

const config = require('@commitlint/config-angular');
const types = config.rules['type-enum'][2];

config.rules['type-enum'][2] = ['chore'].concat(types);

module.exports = config;
