@echo off
protoc --proto_path=./ --java_out=app/src/main/java message.proto
echo MessageProto generated..
echo Press any key to exit..
pause > /dev/null