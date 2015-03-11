PATHTOROOT = ../../..

OBJS = \
	GeometryOfSlice.class \
	GeometryOfVolume.class \
	LocalizerPoster.class \
	LocalizerPosterFactory.class \
	ProjectSlice.class \
	IntersectSlice.class \
	IntersectVolume.class
	
all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../geometry.`date '+%Y%m%d'`.tar.gz

