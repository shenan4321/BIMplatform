'use strict';
var Dlb ={};

(function () {

    var hash = location.hash || '';
    if (hash = hash.match(/^(.*)(\#wechat_redirect|\%23wechat_redirect)/)) { location.hash = hash[1] }

    Dlb.Config = {
        /*timestamp: '2015072404',
        automask: true,
        autotips: true,
        app: {
            code: 'M',
            dir: 'modules/',
            schoolmanager: 'school.admin,school.master,school.vice_master'
        },
        service: {},
        map: [],
        emojis: {}*/
    };

    Dlb.Config.map = [
        { path: '/', view: 'upload.html', viewUrl: 'upload.html', styles: 'upload', scripts: 'upload', modules: 'upload' },
        { path: '/webscoket', view: 'webscoket.html', viewUrl: 'webscoket.html',  scripts: ['https://cdnjs.cloudflare.com/ajax/libs/socket.io/1.7.3/socket.io.js','webscoket'], modules: 'webscoket'},
        { path: '/prolist', view: 'prolist.html', viewUrl: 'prolist.html',  scripts: ['../assets/js/isotope.pkgd.min.js','prolist'], modules: 'prolist'}
    ];

}());
