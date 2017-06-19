var gulp = require('gulp'),
minifycss = require('gulp-minify-css'),
concat = require('gulp-concat'),
uglify = require('gulp-uglify'),
rename = require('gulp-rename'),
htmlreplace = require('gulp-html-replace');

var paths={
    js1:["public/js/progress.js","public/js/websoketArrayBuffer.js"],
    js2:["public/js/ifc/Constants.js","public/js/ifc/DataInputStreamReader.js","public/js/ifc/StringView.js","public/js/ifc/GeometryLoader.js"],
    js3:["public/js/app/sceneTree.js","public/js/app/renderBim2.js"],
    js4:["public/js/dlbSvg.js","public/js/app/menu.js","public/js/html5validate.js","public/js/color/jquery.minicolors.js"]
};
var renamejs=['help.js','ifcUtil.js','feature.js','jqueryUtil.js'];
//压缩css
gulp.task('minifycss', function() {
    return gulp.src('public/css/*.css')    //需要操作的文件
        .pipe(concat('dist.min.css'))
        //.pipe(rename({suffix: '.min'}))   //rename压缩后的文件名
        .pipe(minifycss())   //执行压缩
        .pipe(gulp.dest('public/css'));   //输出文件夹------------------------------------------------------------------------------------
});
//压缩，合并  js

var t = 0;
for(var x in paths){
    t++;
    (function(t){
        gulp.task('minifyjs'+t, function(sa) {
            console.log(t);
            return gulp.src(paths['js'+t])      //需要操作的文件
                .pipe(concat(renamejs[t-1]))    //合并所有js到main.js
                .pipe(gulp.dest('js'))       //输出到文件夹
                .pipe(uglify())    //压缩
                .pipe(gulp.dest('public/dist/js'));  //输出
        });
    })(t)
}


gulp.task('replace', function() {
    var a={};
    for (var i=0;i<renamejs.length;i++){
        a['js'+(i+1)]= 'public/dist/js/'+renamejs[i];
    }
    a.css = 'public/css/dist.min.css';
    gulp.src('modelView.html')
        .pipe(htmlreplace(a))
        .pipe(gulp.dest(''));
});
//默认命令，在cmd中输入gulp后，执行的就是这个任务(压缩js需要在检查js之后操作)
gulp.task('default',function() {

    gulp.start('minifycss');
    var t= 0;
    for(var x in paths){
        t++;
        gulp.start("minifyjs"+t);
    }
    gulp.start('replace');
});