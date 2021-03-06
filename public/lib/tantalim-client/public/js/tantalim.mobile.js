'use strict';
// Source: public/js/mobile/_app.js
// Not sure if they were needed, but removed... 'mobile-angular-ui.touch', 'mobile-angular-ui.scrollable'
angular.module('tantalim.mobile', ['tantalim.common', 'ngRoute', 'mobile-angular-ui']);

// Source: public/js/mobile/main.js
angular.module('tantalim.mobile')
    .config(['$routeProvider', function ($routeProvider) {
        var pageName = window.pageName;
        $routeProvider.
            when('/', {
                templateUrl: '/m/' + pageName + '/list',
                controller: 'PageController'
            }).
            when('/detail/:subPage/:id', {
                templateUrl: '/m/' + pageName + '/detail',
                controller: 'PageController'
            }).
            when('/list/:subPage', {
                templateUrl: '/m/' + pageName + '/list',
                controller: 'PageController'
            }).
            otherwise({
                redirectTo: '/'
            });
    }
    ])
;

// Source: public/js/mobile/pageController.js
angular.module('tantalim.mobile')
    .controller('PageController',
    function ($scope, $routeParams, Global, ModelData, ModelCursor, ModelSaver, PageService) {
        $scope.loading = true;
        if (ModelData.error) {
            $scope.serverStatus = '';
            $scope.serverError = ModelData.error;
            return;
        }

        function attachModelCursorToScope() {
            $scope.ModelCursor = ModelCursor;
            $scope.current = ModelCursor.current;
            $scope.action = ModelCursor.action;
        }

        if (!Global.pageLoaded) {
            Global.pageLoaded = true;
            $scope.serverStatus = 'Loading...';
            $scope.serverError = '';
            PageService.readModelData(ModelData.page.modelName)
                .then(function (d) {
                    $scope.serverStatus = '';
                    $scope.loading = false;
                    if (d.status !== 200) {
                        $scope.serverError = 'Failed to reach server. Try refreshing.';
                        return;
                    }
                    if (d.data.error) {
                        $scope.serverError = 'Error reading data from server: ' + d.data.error;
                        return;
                    }
                    ModelCursor.setRoot(ModelData.model, d.data);
                    attachModelCursorToScope();
                });
        } else {
            attachModelCursorToScope();
            $scope.loading = false;
            $scope.serverStatus = '';
        }

        $scope.staticContent = ModelData.staticContent;
        $scope.currentModel = ModelData.currentModel;
        Global.modelName = $scope.currentModel;

        $scope.currentPage = 'list-' + ModelData.page.id;
        if ($routeParams.subPage) {
            if ($routeParams.id) {
                $scope.currentPage = 'detail-' + $routeParams.subPage;
                ModelCursor.action.choose($routeParams.subPage, $routeParams.id);
            } else {
                $scope.currentPage = 'list-' + $routeParams.subPage;
            }
        }

        $scope.isActive = function (menuItem) {
            return menuItem === Global.pageName;
        };

        $scope.rowChanged = function (thisInstance) {
            ModelCursor.change(thisInstance);
        };

        $scope.save = function () {
            $scope.serverStatus = 'Saving...';
            ModelSaver.save(ModelData.model, ModelCursor.root, function (status) {
                $scope.serverStatus = status;
                if (!status) {
                    ModelCursor.dirty = false;
                }
            });
        };
    }
);
// Source: public/js/common/_app.js
/* global angular */

angular.module('tantalim.common', []);

// Source: public/js/common/global.js
/* global angular */

angular.module('tantalim.common')
    .factory('Global', [
        function () {
            return {
                pageName: window.pageName,
                modelName: window.pageName,
                user: window.user,
                authenticated: !!window.user
            };
        }
    ]);

// Source: public/js/common/guid.js
/* global angular */

angular.module('tantalim.common')
    .factory('GUID', function () {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }

        return function () {
            return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                s4() + '-' + s4() + s4() + s4();
        };
    });

// Source: public/js/common/logger.js
/**
 * Logger.error("Error here");
 * Logger.log({
 *  message: "Or like this",
 *  type: Logger.TYPE.WARN,
 *  source: 'ModelCursor.js Line 123',
 *  details: 'Should fix it here.'
 * });
 */
angular.module('tantalim.common')
    .factory('Logger', [
        function () {

            var _self = {
                TYPE: {INFO: 'info', WARN: 'warn', ERROR: 'warn', DEBUG: 'debug'},
                history: [],
                log: function (content, messageType) {
                    content = normalizeContent(content);
                    content.type = messageType || _self.TYPE.INFO;

                    if (content.type === _self.TYPE.ERROR) {
                        _self._error = content.message
                    } else {
                        _self._status = content.message
                    }

                    _self.history.push(content);
                },
                info: function (content) {
                    _self.log(content);
                },
                warn: function (content) {
                    _self.log(content, _self.TYPE.WARN);
                },
                debug: function (content) {
                    _self.log(content, _self.TYPE.DEBUG);
                },
                error: function (content) {
                    _self.log(content, _self.TYPE.ERROR);
                },
                getStatus: function (status) {
                    return _self._status;
                },
                getError: function () {
                    return _self._error;
                }
            };

            function normalizeContent(content) {
                if (typeof content === 'string') {
                    return {message: content};
                } else return content;
            }

            return _self;
        }
    ]);
// Source: public/js/common/modelCursor.js
/* global _ */
/* global angular */

angular.module('tantalim.common')
    .factory('ModelCursor', ['$filter', '$log', 'GUID',
        function ($filter, $log, GUID) {
            var rootSet;
            var current;
            var modelMap;

            var clear = function () {
                rootSet = null;
                current = {};
                modelMap = {};
            };
            clear();

            var fillModelMap = function (model, parentName) {
                modelMap[model.name] = model;
                model.parent = parentName;
                _.forEach(model.children, function (childModel) {
                    fillModelMap(childModel, model.name);
                });
            };

            var STATE = {
                NO_CHANGE: 'NO_CHANGE',
                DELETED: 'DELETED',
                INSERTED: 'INSERTED',
                UPDATED: 'UPDATED',
                CHILD_UPDATED: 'CHILD_UPDATED'
            };

            /**
             *
             * @param model
             * @param row
             * @param nodeSet
             */
            var SmartNodeInstance = function (model, row, nodeSet) {
                //$log.debug('Adding SmartNodeInstance id:%s for model `%s` onto set ', row.id, model.name, nodeSet);
                var defaults = {
                    _type: 'SmartNodeInstance',
                    /**
                     * Unique identifier for this instance
                     */
                    id: null,
                    /**
                     * The map of columns and values for this instance
                     */
                    data: {},
                    /**
                     * reference to the parent SmartNodeSet that contains this instance
                     */
                    nodeSet: null,
                    /**
                     * map of SmartNodeInstances representing the children of this node
                     */
                    childModels: {},
                    getChild: function(name) {
                        var childModel = this.childModels[name];
                        if (childModel === undefined) {
                            console.warn('Child model ' + name + ' doesn\'t exist.');
                        }
                        return childModel;
                    },
                    state: STATE.NO_CHANGE,

                    delete: function () {
                        this.state = STATE.DELETED;
                    },
                    isDirty: function() {
                        //console.info("Checking this.state" + this.state + " for " + this.id);
                        return this.state !== STATE.NO_CHANGE;
                    },
                    toggle: function (fieldName, required) {
                        var currentValue = this.data[fieldName];
                        var newValue = true;
                        if (currentValue === true) newValue = false;
                        if (currentValue === undefined) newValue = true;
                        if (currentValue === false) {
                            if (required) newValue = true;
                            else newValue = null;
                        }
                        console.info("Setting ", fieldName, newValue);
                        this.update(fieldName, newValue);
                    },
                    getField: function(fieldName) {
                        var model = modelMap[this.nodeSet.model.modelName];
                        return model.fields[fieldName];
                    },
                    getValue: function (fieldName) {
                        //console.info('finding value for fieldName: ', fieldName, this.data);

                        if (this.data.hasOwnProperty(fieldName)) {
                            return this.data[fieldName];
                        }

                        if (this.getField(fieldName)) {
                            // This model has the field but it's empty
                            return null;
                        }

                        if (this.nodeSet.parentInstance) {
                            return this.nodeSet.parentInstance.getValue(fieldName);
                        }

                        // We could consider checking child models first before dying
                        throw new Error('Cannot find field called ' + fieldName);
                    },
                    setValue: function (fieldName, newValue, force) {
                        console.info('update ' + fieldName + ' to ' + newValue);
                        force = force || false;
                        var field = modelMap[nodeSet.model.modelName].fields[fieldName];
                        if (!field) {
                            console.error("Failed to find field named " + fieldName + " in ", modelMap[nodeSet.model.modelName].fields);
                        }
                        if (!field.updateable && !force) {
                            return;
                        }
                        // TODO get field defaults to work
                        if (fieldName) {
                            if (fieldName === 'TableName' && newValue) {
                                this.data.TableSQL = 'app_' + this.data[fieldName].toLowerCase();
                            }
                            if (newValue !== undefined) {
                                this.data[fieldName] = newValue;
                            }
                        }
                        if (this.state === STATE.NO_CHANGE || this.state === STATE.CHILD_UPDATED) {
                            this.state = STATE.UPDATED;
                            this.updateParent();
                        }
                    },
                    update: function (fieldName, newValue, force) {
                        console.warn('update is deprecated. Use setValue');
                        this.setValue(fieldName, newValue, force);
                    },
                    updateParent: function () {
                        if (!this.nodeSet) return;
                        var parent = this.nodeSet.parentInstance;
                        if (parent && parent.state === STATE.NO_CHANGE) {
                            parent.state = STATE.CHILD_UPDATED;
                            parent.updateParent();
                        }

                    },
                    isFieldEditable: function() {
                        //.state !== "INSERTED" && !currentField.updateable

                        return true;
                    }
                };

                var newInstance = _.defaults(row, defaults);
                newInstance.nodeSet = nodeSet;

                function setFieldDefault(field, row) {
                    var defaultValue;

                    if (field.valueDefault !== null) {
                        console.info(field.name + field.valueDefault);

                        defaultValue = field.valueDefault;
                        switch (field.dataType) {
                            case "Boolean":
                                defaultValue = (defaultValue === 'true');
                                break;
                        }
                    }
                    if (field.fieldDefault !== null) {
                        defaultValue = row.getValue(field.fieldDefault);
                    }
                    if (field.functionDefault !== null) {
                        defaultValue = eval(field.functionDefault);
                    }
                    if (defaultValue !== undefined) {
                        $log.debug('defaulted ' + field.name + ' to ' + defaultValue + ' of type ' + typeof defaultValue);
                        row.data[field.name] = defaultValue;
                    }
                }

                if (row.id === null) {
                    //$log.debug('id is null, so assume record is newly inserted', row);
                    newInstance.state = STATE.INSERTED;
                    newInstance.id = GUID();
                    _.forEach(model.fields, function (field) {
                        setFieldDefault(field, row);
                    });
                }

                newInstance.addChildModel = function (childModelName, childDataSet) {
                    //$log.debug('addChildModel ', childModelName, childDataSet);
                    var smartSet = new SmartNodeSet(modelMap[childModelName], childDataSet, newInstance, nodeSet.depth + 1);
                    newInstance.childModels[childModelName] = smartSet;
                };

                _.forEach(model.children, function (childModel) {
                    if (row.children) {
                        newInstance.addChildModel(childModel.name, row.children[childModel.name]);
                    } else {
                        newInstance.addChildModel(childModel.name, []);
                    }
                });

                //$log.debug('Done creating newInstance');
                return newInstance;
            };

            /**
             *
             * @param model
             * @param data
             * @param parentInstance
             * @param depth
             */
            var SmartNodeSet = function (model, data, parentInstance, depth) {
                //$log.debug('Adding SmartNodeSet for ' + model.name + ' at depth ' + depth);
                var defaults = {
                    _type: 'SmartNodeSet',
                    model: {
                        /**
                         * Name of the model
                         */
                        modelName: null,
                        /**
                         * Reference to the parent instance if any
                         */
                        parentInstance: null,
                        /**
                         * String - name of column to sort by
                         * function - function to apply to row for sorting
                         */
                        orderBy: null,
                        /**
                         * Array of strings representing the name of each child model
                         */
                        childModels: []
                    },
                    /**
                     * The parent SmartNodeInstance of this SmartNodeSet
                     * null if this instance is root
                     */
                    parent: {
                        instance: null,
                        model: null
                    },
                    /**
                     * Array of SmartNodeInstances
                     */
                    rows: [],
                    /**
                     * Array of SmartNodeInstances
                     */
                    deleted: [],
                    depth: depth || 0, // Will probably remove level
                    index: -1,

                    sort: function (field, direction) {
                        this.rows = $filter('orderBy')(this.rows, 'data.' + field, direction);
                    },
                    sortReverse: function () {
                        this.sort(true);
                    },
                    moveTo: function (index) {
                        if (index < 0) {
                            return;
                        }
                        if (index >= this.rows.length) {
                            return;
                        }
                        this.index = index;
                        self.resetCurrents(this);
                    },
                    movePrevious: function () {
                        this.moveTo(this.index - 1);
                    },
                    moveNext: function () {
                        this.moveTo(this.index + 1);
                    },
                    hasPrevious: function () {
                        return this.index > 0;
                    },
                    hasNext: function () {
                        return this.index + 1 < this.rows.length;
                    },
                    findIndex: function (id) {
                        return _.findIndex(this.rows, function (row) {
                            return row.id === id;
                        });
                    },
                    find: function(matcher) {
                        return _.find(this.rows, matcher);
                    },
                    isEmpty: function() {
                        return !(this.rows.length > 0);
                    },
                    max: function(fieldName, lowest) {
                        if (this.isEmpty()) {
                            return lowest;
                        }
                        var value = _.max(this.rows, function(row) {
                            return Number(row.data[fieldName]);
                        });
                        if (value) return Number(value.data[fieldName]);
                        else return lowest;
                    },
                    min: function(fieldName, highest) {
                        if (this.isEmpty()) {
                            return highest;
                        }
                        var value = _.min(this.rows, function(row) {
                            return Number(row.data[fieldName]);
                        });
                        if (value) return Number(value.data[fieldName]);
                        else return highest;
                    },
                    isDirty: function() {
                        if (this.deleted && this.deleted.length > 0) return true;

                        var dirtyRow = _.find(this.rows, function(row) {
                            return row.isDirty();
                        });
                        return !_.isEmpty(dirtyRow);
                    },
                    delete: function (index) {
                        // console.info('deleting on set with index =', index);
                        var row = this.rows[index];
                        if (row.state !== STATE.INSERTED) {
                            // Only delete previously saved records
                            this.deleted.push(row);
                            row.updateParent();
                        }
                        this.rows.splice(index, 1);
                        self.clearCurrentChildren(this.model.modelName);
                    },
                    deleteEnabled: function() {
                        return this.getInstance() !== null;
                    },
                    getInstance: function (index) {
                        if (!this.rows || this.rows.length === 0) {
                            this.index = -1;
                            return null;
                        }
                        if (this.index < 0) {
                            this.index = 0;
                        }
                        index = index || this.index || 0;
                        return this.rows[index];
                    },
                    reloadFromServer: function (newData) {
                        $log.debug('reloadFromServer with returned data', newData);
                        $log.debug('this set', this);

                        _.forEach(this.rows, function (row) {
                            var modifiedRow;
                            switch (row.state) {
                                case STATE.INSERTED:
                                    modifiedRow = _.find(newData, function (newRow) {
                                        return newRow.tempID === row.id;
                                    });
                                    $log.debug('matching inserted row', row, modifiedRow);
                                    if (modifiedRow) {
                                        row.state = STATE.NO_CHANGE;
                                        row.data = modifiedRow.data;
                                        row.id = modifiedRow.id;
                                    } else {
                                        console.error('Failed to find matching inserted row', row);
                                    }
                                    break;
                                case STATE.UPDATED:
                                case STATE.CHILD_UPDATED:
                                    modifiedRow = _.find(newData, function (newRow) {
                                        return newRow.id === row.id;
                                    });
                                    $log.debug('matching update or child_updated row', row, modifiedRow);
                                    if (modifiedRow) {
                                        row.state = STATE.NO_CHANGE;
                                        if (row.state === STATE.UPDATED) {
                                            // Don't bother replacing the data if it was just the child data updated
                                            row.data = modifiedRow.data;
                                        }
                                    } else {
                                        console.error('Failed to find matching updated row', row);
                                    }
                                    break;
                            }
                            if (row.childModels && modifiedRow && modifiedRow.children) {
                                //console.info(row.childModels);
                                //console.info(modifiedRow);
                                _.forEach(row.childModels, function (child, childName) {
                                    //console.info(childName);
                                    var childDataFromServer = modifiedRow.children[childName];
                                    if (childDataFromServer) {
                                        row.childModels[childName].reloadFromServer(childDataFromServer);
                                    }
                                });
                            }
                        });

                        this.deleted = [];
                    }
                };

                var newSet = _.defaults({}, defaults);
                newSet.model.modelName = model.name;
                newSet.model.orderBy = model.orderBy;
                newSet.parentInstance = parentInstance;
                newSet.insert = function (values) {
                    $log.debug('Inserting new instance with model');
                    var smartInstance = new SmartNodeInstance(model, {data: values || {}}, newSet);
                    newSet.rows.push(smartInstance);
                    newSet.index = newSet.rows.length - 1;
                    smartInstance.updateParent();
                    self.resetCurrents(newSet);
                    return smartInstance;
                };

                if (angular.isArray(data)) {
                    _.forEach(data, function (row) {
                        var smartInstance = new SmartNodeInstance(model, row, newSet);
                        newSet.rows.push(smartInstance);
                    });
                    self.resetCurrents(newSet);
                }

                return newSet;
            };

            var self = {
                root: rootSet,
                current: current,
                setRoot: function (model, data) {
                    $log.debug('Setting Root data');
                    $log.debug(model);
                    $log.debug(data);
                    clear();
                    self.current = current;
                    if (_.isEmpty(model)) {
                        console.warn("setRoot called with empty model, exiting");
                        return;
                    }
                    fillModelMap(model);
                    rootSet = new SmartNodeSet(model, data);
                    self.root = rootSet;
                    self.resetCurrents(rootSet);
                },
                getCurrentSet: function (modelName) {
                    if (!_.isEmpty(self.current) && !self.current[modelName]) {
                        //console.warn('getCurrentSet is empty', modelName);
                        //console.warn('current = ', self.current);
                    }
                    return self.current[modelName];
                },
                resetCurrents: function (thisSet) {
                    if (!thisSet || thisSet._type !== 'SmartNodeSet') {
                        throw new Error('resetCurrents() requires a SmartNodeSet but got', thisSet);
                    }

                    var modelName = thisSet.model.modelName;

                    var thisModel = modelMap[modelName];
                    self.clearCurrentChildren(thisModel);

                    self.current[modelName] = thisSet;

                    var nextInstance = thisSet.getInstance();

                    if (thisModel && thisModel.children) {
                        _.forEach(thisModel.children, function (childModel) {
                            if (nextInstance && nextInstance.childModels) {
                                var childSet = nextInstance.childModels[childModel.name];
                                if (childSet) {
                                    self.resetCurrents(childSet);
                                }
                            }
                        });
                    }
                },
                clearCurrentChildren: function (model) {
                    if (!model.children) {
                        return;
                    }
                    _.forEach(model.children, function (childModel) {
                        current[childModel.name] = null;
                        self.clearCurrentChildren(childModel);
                    });
                },
                dirty: function () {
                    if (!rootSet) return false;
                    return rootSet.isDirty();
                },
                toConsole: function () {
                    console.log('ModelCursor.rootSet', self.root);
                    console.log('ModelCursor.modelMap', modelMap);
                    console.log('ModelCursor.current', self.current);
                }
            };

            return self;
        }
    ]);
// Source: public/js/common/modelSaver.js
/* global _ */
/* global angular */

angular.module('tantalim.common')
    .factory('ModelSaver', ['$http', '$log',
        function ($http, $log) {
            var rootSet;

            var _self = {
                convertToDto: function (model, dataSet) {
                    var modelName = model.name;
                    //$log.debug('Starting convertToDto for model ' + modelName);
                    //$log.debug(model);
                    //$log.debug(dataSet);

                    var convertSmartNodeInstanceToInsert = function (instance) {
                        return {
                            state: instance.state,
                            tempID: instance.id,
                            data: instance.data
                        };
                    };

                    var convertSmartNodeInstanceToUpdate = function (instance) {
                        return {
                            state: instance.state,
                            id: instance.id,
                            data: instance.data
                        };
                    };

                    var convertSmartNodeInstanceToDelete = function (instance) {
                        return {
                            state: 'DELETED',
                            id: instance.id
                        };
                    };

                    var addChildrenDtoToRow = function (parentInstance, model, instance) {
                        var dirtyChildren = false;

                        parentInstance.children = {};

                        _.forEach(model.children, function (childModel) {
                            var childModelName = childModel.name;
                            var dtoRows = _self.convertToDto(childModel, instance.childModels[childModelName]);
                            if (dtoRows.length > 0) {
                                parentInstance.children[childModelName] = dtoRows;
                                dirtyChildren = true;
                            }
                        });

                        if (!dirtyChildren) {
                            delete parentInstance.children;
                        }
                    };

                    var dtoRows = [];
                    if (!dataSet) {
                        $log.debug('dataSet is empty so exit');
                        return dtoRows;
                    }
                    $log.debug(dataSet);

                    _.forEach(dataSet.rows, function (row) {
                        var dtoRow = {};

                        if (row.state === 'INSERTED') {
                            dtoRow = convertSmartNodeInstanceToInsert(row);
                        } else if (row.state === 'UPDATED') {
                            dtoRow = convertSmartNodeInstanceToUpdate(row);
                        } else if (row.state === 'CHILD_UPDATED') {
                            dtoRow = convertSmartNodeInstanceToUpdate(row);
                        } else {
                            dtoRow = null;
                        }
                        if (dtoRow) {
                            dtoRows.push(dtoRow);
                            addChildrenDtoToRow(dtoRow, model, row);
                        }
                    });
                    _.forEach(dataSet.deleted, function (row) {
                        var dtoRow = convertSmartNodeInstanceToDelete(row);
                        dtoRows.push(dtoRow);
                        addChildrenDtoToRow(dtoRow, model, row);
                    });

                    return dtoRows;
                },

                sendData: function (parentModelName, dto, success) {
                    $log.debug('sendData()...');
                    $log.debug(dto);

                    $http.post('/data/' + parentModelName, dto).
                        success(function (data, status) {
                            $log.debug('Returned success');
                            $log.debug(status);
                            if (status === 200) {
                                if (data.error) {
                                    success('Failed to save data ' + data.error.message);
                                } else {
                                    rootSet.reloadFromServer(data);
                                    success('');
                                }
                            } else {
                                success('Failed ' + status);
                                console.error(status);
                                console.error(data);
                            }
                        }).
                        error(function (data, status) {
                            if (status === 404) {
                                success('Server not found. Check your Internet connection and try again.');
                            } else {
                                success('Server returned an unknown ' + status + ' error');
                            }
                        });
                },

                save: function (model, _rootSet, success) {
                    rootSet = _rootSet;
                    $log.debug('Starting ModelSaver.save');
                    var dtoRows = _self.convertToDto(model, rootSet);
                    _self.sendData(model.name, dtoRows, success);
                }
            };
            return _self;
        }
    ]);
// Source: public/js/common/pageService.js
/* global angular */

angular.module('tantalim.common')
    .factory('PageService', function ($http) {
        var self = {
            readModelData: function (modelName, filterString, pageNumber) {
                var url = '/data/' + modelName + '?';
                if (filterString) {
                    url += 'filter=' + filterString;
                }
                if (pageNumber) {
                    if (filterString) {
                        url += '&';
                    }
                    url += 'page=' + pageNumber;
                }
                return self.readUrl(url);
            },
            readUrl: function (url) {
                return $http.get(url);
            }
        };
        return self;
    });
