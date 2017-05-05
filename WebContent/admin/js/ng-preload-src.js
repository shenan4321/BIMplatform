/*! ng-preload-src - v 0.0.4 - Tue Apr 29 2014 17:48:17 GMT+0800 (CST)
 * https://github.com/tomchentw/ng-preload-src
 * Copyright (c) 2014 [tomchentw](https://github.com/tomchentw);
 * Licensed [MIT](http://tomchentw.mit-license.org)
 */
/*global angular:false*/
(function(angular, bind, noop){
  angular.module('ng-preload-src', []).factory('$image', ['$window', '$q'].concat(function($window, $q){
    return {
      preload: function(url){
        var defer, image;
        defer = $q.defer();
        image = new $window.Image;
        image.src = url;
        angular.element(image).on('load', bind(defer, defer.resolve, url));
        return defer.promise;
      }
    };
  })).directive('preloadWithDefaultSrc', ['$interpolate', '$image', '$injector', '$timeout'].concat(function($interpolate, $image, $injector, $timeout){
    var PreloadCtrl;
    PreloadCtrl = (function(){
      PreloadCtrl.displayName = 'PreloadCtrl';
      var prototype = PreloadCtrl.prototype, constructor = PreloadCtrl;
      PreloadCtrl.cfpLoadingBar = $injector.get('cfpLoadingBar') || {
        start: noop,
        set: noop,
        complete: noop
      };
      PreloadCtrl.reqsTotal = PreloadCtrl.reqsCompleted = 0;
      PreloadCtrl.startTimeout = void 8;
      prototype.onSrcChanged = function(newUrl){
        this.$attrs.$set('ngSrc', this.$attrs.preloadWithDefaultSrc);
        this.lastUrl = newUrl;
        this.$scope.$root.$broadcast('cfpLoadingBar:loading', {
          url: newUrl
        });
        if (0 === constructor.reqsTotal) {
          constructor.startTimeout = $timeout(function(){
            constructor.cfpLoadingBar.start();
          }, 100);
        }
        constructor.reqsTotal++;
        constructor.cfpLoadingBar.set(constructor.reqsCompleted / constructor.reqsTotal);
        $image.preload(newUrl).then(this.onImagePreloaded);
      };
      prototype.onImagePreloaded = function(newUrl){
        if (this.lastUrl === newUrl) {
          this.$attrs.$set('ngSrc', newUrl);
        }
      };
      prototype.onLoadEvent = function(){
        if (this.lastUrl !== this.$attrs.ngSrc) {
          return;
        }
        constructor.reqsCompleted++;
        this.$scope.$root.$broadcast('cfpLoadingBar:loaded', {
          url: this.lastUrl
        });
        if (constructor.reqsCompleted >= constructor.reqsTotal) {
          $timeout.cancel(constructor.startTimeout);
          constructor.cfpLoadingBar.complete();
          constructor.reqsCompleted = constructor.reqsTotal = 0;
        } else {
          constructor.cfpLoadingBar.set(constructor.reqsCompleted / constructor.reqsTotal);
        }
      };
      PreloadCtrl.$inject = ['$scope', '$attrs'];
      function PreloadCtrl($scope, $attrs){
        this.$scope = $scope;
        this.$attrs = $attrs;
        this.lastUrl = void 8;
        this.onImagePreloaded = bind(this, this.onImagePreloaded);
        this.onLoadEvent = bind(this, this.onLoadEvent);
        $scope.$watch($interpolate($attrs.preloadSrc), bind(this, this.onSrcChanged));
      }
      return PreloadCtrl;
    }());
    function postLinkFn($scope, $element, $attrs, preloadCtrl){
      $element.on('load', preloadCtrl.onLoadEvent);
    }
    return {
      controller: PreloadCtrl,
      require: 'preloadWithDefaultSrc',
      compile: function(tElement, tAttrs){
        tAttrs.$set('preloadSrc', tAttrs.ngSrc);
        tAttrs.$set('ngSrc', tAttrs.preloadWithDefaultSrc);
        return postLinkFn;
      }
    };
  }));
}.call(this, angular, angular.bind, angular.noop));