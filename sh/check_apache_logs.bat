set TARGET_DIR=C:\Tools\Apache\logs
cd %%TARGET_DIR

rem �J��Ԃ����{
FOR /L %%i IN (1,1,1000000) Do (

    rem ���������t�@�C�����ƂɎ��{
    for %%f in (access*) do dir %%f
   
  )
)