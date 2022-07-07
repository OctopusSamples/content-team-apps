var gulp = require('gulp');
var imagemin = require('gulp-imagemin');
var pngquant = require('imagemin-pngquant');
var cache = require('gulp-cache');
var cp = require('child_process');
var browserSync = require('browser-sync');

var jekyll   = process.platform === 'win32' ? 'jekyll.bat' : 'jekyll';

// Build the Jekyll Site
gulp.task('jekyll-build', function (done) {
    return cp.spawn( jekyll , ['build'], {stdio: 'inherit'})
        .on('close', done);
});

// Rebuild Jekyll and page reload
gulp.task('jekyll-rebuild', ['jekyll-build'], function () {
    browserSync.reload();
});

// Wait for jekyll-build, then launch the Server
gulp.task('browser-sync', ['img', 'jekyll-build'], function() {
    browserSync({
        server: {
            baseDir: '_site'
        },
        notify: false
    });
});

// Compression images
gulp.task('img', function() {
	return gulp.src('assets/img/**/*')
		.pipe(cache(imagemin({
			interlaced: true,
			progressive: true,
			svgoPlugins: [{removeViewBox: false}],
			use: [pngquant()]
		})))
    .pipe(gulp.dest('_site/assets/img'))
    .pipe(browserSync.reload({stream:true}));
});

// Watch scss, html, img files
gulp.task('watch', function () {
    gulp.watch('assets/js/**/*.js', ['jekyll-rebuild']);
    gulp.watch('assets/img/**/*', ['img']);
    gulp.watch(['*.html', '_layouts/*.html', '_includes/*.html', '_pages/*.html', '_posts/*'], ['jekyll-rebuild']);
});

//  Default task
gulp.task('default', ['browser-sync', 'watch']);
