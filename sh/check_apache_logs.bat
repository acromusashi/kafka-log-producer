set TARGET_DIR=C:\Tools\Apache\logs
cd %%TARGET_DIR

rem ŒJ‚è•Ô‚µŽÀŽ{
FOR /L %%i IN (1,1,1000000) Do (

    rem ”­Œ©‚µ‚½ƒtƒ@ƒCƒ‹‚²‚Æ‚ÉŽÀŽ{
    for %%f in (access*) do dir %%f
   
  )
)