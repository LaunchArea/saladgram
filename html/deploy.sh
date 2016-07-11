if [ $# == 0 ]; then
    echo "./deploy.sh [test | operation]"
    exit 0
fi

if [ $1 == "test" ]; then
    TARGET=/usr/share/nginx/html/test
elif [ $1 == "operation" ]; then
    TARGET=/usr/share/nginx/html
else
    echo "Invalid target"
    exit 0
fi

echo "TARGET = $TARGET"

sudo cp *.html $TARGET
sudo cp -R assets $TARGET
sudo cp -R css $TARGET
sudo cp -R img $TARGET
sudo cp -R images $TARGET
sudo cp -R js $TARGET
sudo cp -R templates $TARGET
