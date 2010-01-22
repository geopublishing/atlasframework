#!/bin/bash

# LGPL (c) Stefan Krueger

chmod -R +x /home/stefan/Desktop/ATLAS/AtlasExported/JWS

# Changing to the default export directory
cd /home/stefan/Desktop/ATLAS/AtlasExported/JWS && rm *.tmp 

# Replacing localhost with www.wikisquare.de
cp atlasViewer.jnlp atlasViewerLocal.jnlp
cat atlasViewer.jnlp | sed s/localhost/www.wikisquare.de/g >  atlasViewer.jnlp2 
mv atlasViewer.jnlp2 atlasViewer.jnlp 

# Rsyncing
rsync -crv * prodi:atlas
# The runnable JWS Atlas is now online... 

echo Create and upload the new ZIP Version
cd ../DISK
ZIPNAME=atlas_$(date +%Y%m%d).zip
rm $ZIPNAME
#echo "@echo off" > atlas.bat
#echo "cd Atlas_Export_09_03_05" >> atlas.bat
#echo "start.bat" >> atlas.bat

#echo "#!/bin/bash" > atlas.sh
#echo "cd Atlas_Export_09_03_05" >> atlas.sh
#echo "start.sh" >> atlas.sh

zip -9r $ZIPNAME *
rsync -v $ZIPNAME prodi:atlas

echo UPDATE THE LINK atlas.zip
rm atlas.zip
ln $ZIPNAME atlas.zip -s
rsync -vl atlas.zip prodi:atlas
