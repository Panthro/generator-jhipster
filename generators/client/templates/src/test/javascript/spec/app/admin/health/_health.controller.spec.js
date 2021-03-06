'use strict';

describe('Controller Tests', function () {

    describe('HealthController', function () {
        var $scope; // actual implementations
        var createController; // local utility functions

        beforeEach(inject(function ($injector) {
            $scope = $injector.get('$rootScope').$new();
            var locals = {
                '$scope': $scope
            };
            createController = function() {
                $injector.get('$controller')('HealthController as vm', locals);
            };
            createController();
        }));

        describe('isHealthObject and hasSubSystem', function () {
            it('should verify empty object is not a health property leaf', function () {
                expect($scope.vm.isHealthObject({})).toBe(false);
                expect($scope.vm.hasSubSystem({})).toBe(false);
            });

            it('should verify object with property status and no subsystems is a health property leaf', function () {
                var healthObject = {
                    'status': 'UP'
                };
                expect($scope.vm.isHealthObject(healthObject)).toBe(true);
                expect($scope.vm.hasSubSystem(healthObject)).toBe(false);
            });

            it('should verify that object property status and unrecognized objects is a health property leaf', function () {
                var healthObject = {
                    'status': 'UP',
                    'subsystem': {
                        'hello': 'UP'
                    }
                };
                expect($scope.vm.isHealthObject(healthObject)).toBe(true);
                expect($scope.vm.hasSubSystem(healthObject)).toBe(false);
            });

            it('should verify object with property status but with subsystems is NOT a health property leaf', function () {
                var healthObject = {
                    'status': 'UP',
                    'subsystem': {
                        'status': 'UP'
                    }
                };
                expect($scope.vm.isHealthObject(healthObject)).toBe(true);
                expect($scope.vm.hasSubSystem(healthObject)).toBe(true);
            });

        });

        describe('transformHealthData', function () {
            it('should flatten empty health data', function () {
                var data = {};
                var expected = [];
                expect($scope.vm.transformHealthData(data)).toEqual(expected);
            });

            it('should flatten health data with no subsystems', function () {
                var data = {
                    'status': 'UP',
                    'db': {
                        'status': 'UP',
                        'database': 'H2',
                        'hello': '1'
                    },
                    'mail': {
                        'status': 'UP',
                        'error': 'mail.a.b.c'
                    }
                };
                var expected = [
                    {
                        'name': 'db',
                        'status': 'UP',
                        'details': {
                            'database': 'H2',
                            'hello': '1'
                        }
                    },
                    {
                        'name': 'mail',
                        'status': 'UP',
                        'error': 'mail.a.b.c'
                    }
                ];
                expect($scope.vm.transformHealthData(data)).toEqual(expected);
            });

            it('should flatten health data with subsystems at level 1, main system has no additional information', function () {
                var data = {
                    'status': 'UP',
                    'db': {
                        'status': 'UP',
                        'database': 'H2',
                        'hello': '1'
                    },
                    'mail': {
                        'status': 'UP',
                        'error': 'mail.a.b.c'
                    },
                    'system': {
                        'status': 'DOWN',
                        'subsystem1': {
                            'status': 'UP',
                            'property1': 'system.subsystem1.property1'
                        },
                        'subsystem2': {
                            'status': 'DOWN',
                            'error': 'system.subsystem1.error',
                            'property2': 'system.subsystem2.property2'
                        }
                    }
                };
                var expected = [
                    {
                        'name': 'db',
                        'status': 'UP',
                        'details': {
                            'database': 'H2',
                            'hello': '1'
                        }
                    },
                    {
                        'name': 'mail',
                        'status': 'UP',
                        'error': 'mail.a.b.c'
                    },
                    {
                        'name': 'system.subsystem1',
                        'status': 'UP',
                        'details': {
                            'property1': 'system.subsystem1.property1'
                        }
                    },
                    {
                        'name': 'system.subsystem2',
                        'status': 'DOWN',
                        'error': 'system.subsystem1.error',
                        'details': {
                            'property2': 'system.subsystem2.property2'
                        }
                    }
                ];
                expect($scope.vm.transformHealthData(data)).toEqual(expected);
            });

            it('should flatten health data with subsystems at level 1, main system has additional information', function () {
                var data = {
                    'status': 'UP',
                    'db': {
                        'status': 'UP',
                        'database': 'H2',
                        'hello': '1'
                    },
                    'mail': {
                        'status': 'UP',
                        'error': 'mail.a.b.c'
                    },
                    'system': {
                        'status': 'DOWN',
                        'property1': 'system.property1',
                        'subsystem1': {
                            'status': 'UP',
                            'property1': 'system.subsystem1.property1'
                        },
                        'subsystem2': {
                            'status': 'DOWN',
                            'error': 'system.subsystem1.error',
                            'property2': 'system.subsystem2.property2'
                        }
                    }
                };
                var expected = [
                    {
                        'name': 'db',
                        'status': 'UP',
                        'details': {
                            'database': 'H2',
                            'hello': '1'
                        }
                    },
                    {
                        'name': 'mail',
                        'status': 'UP',
                        'error': 'mail.a.b.c'
                    },
                    {
                        'name': 'system',
                        'status': 'DOWN',
                        'details': {
                            'property1': 'system.property1'
                        }
                    },
                    {
                        'name': 'system.subsystem1',
                        'status': 'UP',
                        'details': {
                            'property1': 'system.subsystem1.property1'
                        }
                    },
                    {
                        'name': 'system.subsystem2',
                        'status': 'DOWN',
                        'error': 'system.subsystem1.error',
                        'details': {
                            'property2': 'system.subsystem2.property2'
                        }
                    }
                ];
                expect($scope.vm.transformHealthData(data)).toEqual(expected);
            });

            it('should flatten health data with subsystems at level 1, main system has additional error', function () {
                var data = {
                    'status': 'UP',
                    'db': {
                        'status': 'UP',
                        'database': 'H2',
                        'hello': '1'
                    },
                    'mail': {
                        'status': 'UP',
                        'error': 'mail.a.b.c'
                    },
                    'system': {
                        'status': 'DOWN',
                        'error': 'show me',
                        'subsystem1': {
                            'status': 'UP',
                            'property1': 'system.subsystem1.property1'
                        },
                        'subsystem2': {
                            'status': 'DOWN',
                            'error': 'system.subsystem1.error',
                            'property2': 'system.subsystem2.property2'
                        }
                    }
                };
                var expected = [
                    {
                        'name': 'db',
                        'status': 'UP',
                        'details': {
                            'database': 'H2',
                            'hello': '1'
                        }
                    },
                    {
                        'name': 'mail',
                        'status': 'UP',
                        'error': 'mail.a.b.c'
                    },
                    {
                        'name': 'system',
                        'status': 'DOWN',
                        'error': 'show me'
                    },
                    {
                        'name': 'system.subsystem1',
                        'status': 'UP',
                        'details': {
                            'property1': 'system.subsystem1.property1'
                        }
                    },
                    {
                        'name': 'system.subsystem2',
                        'status': 'DOWN',
                        'error': 'system.subsystem1.error',
                        'details': {
                            'property2': 'system.subsystem2.property2'
                        }
                    }
                ];
                expect($scope.vm.transformHealthData(data)).toEqual(expected);
            });
        });

        describe('getModuleName(path, name)', function () {
            it('should show both path and name if defined', function () {
                expect($scope.vm.getModuleName('path', 'name')).toEqual('path' + $scope.vm.separator + 'name');
            });

            it('should show only path if name is not defined', function () {
                expect($scope.vm.getModuleName('path')).toEqual('path');
                expect($scope.vm.getModuleName('path', '')).toEqual('path');
                expect($scope.vm.getModuleName('path', null)).toEqual('path');
            });

            it('should show only name if path is not defined', function () {
                expect($scope.vm.getModuleName(null, 'name')).toEqual('name');
                expect($scope.vm.getModuleName('', 'name')).toEqual('name');
            });

            it('should show empty string if neither name nor path is defined', function () {
                expect($scope.vm.getModuleName()).toEqual('');
            });
        });
    });
});
