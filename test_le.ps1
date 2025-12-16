
$java_files = (gci -Recurse -Include *.java src).FullName

javac -Xdiags:verbose -Xlint:unchecked -d out $java_files


# # run client in parallel shell
# Start-Process pwsh -ArgumentList @(
#     "-NoExit", 
#     "-Command", 
#     "java -cp out testes.Test"
# )

# sleep 1

# java -cp out testes.NioClient
java -cp out testes.Test4

# echo $pwd

# wt --size "190,20" `
#     -d $pwd pwsh -c "java -cp out testes.Test" `; `
#     split-pane -V -d $pwd pwsh -c "sleep 1 && java -cp out testes.Client" `

# wt -d $pwd pwsh -c "java -cp out testes.Test3" `; `
#     split-pane -V -d $pwd pwsh -c "sleep 1 && java -cp out testes.Client" `
