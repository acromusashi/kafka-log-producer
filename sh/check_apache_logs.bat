set TARGET_DIR=C:\Tools\Apache\logs
cd %%TARGET_DIR

rem 繰り返し実施
FOR /L %%i IN (1,1,1000000) Do (

    rem 発見したファイルごとに実施
    for %%f in (access*) do dir %%f
   
  )
)