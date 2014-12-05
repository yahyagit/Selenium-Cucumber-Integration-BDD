'Purpose: ALM Post Execution Email
'Authot - Ram (229673)

'1. Declare and initialize required variables and objects.
'*******************************************************************
Dim gobjFso, gobjMyFile, gblnDebugMode, gobjQtpApp, garrQtpAddins, gblnActivateOK, gstrError
Dim gstrServerURL, gstrDomainName, gstrProjectName, gstrUserName, gstrPassword
Dim gstrTestSet,gstrQCRunControllerFile
Dim objExcel, objWorkBook, objWorkSheet, gstrCurrentDirectory
Dim intRowCount, intRowIterator
Set oShell = CreateObject("WScript.Shell")
Set gobjFso = CreateObject("Scripting.FileSystemObject")

'1.a. Get the arguments passed to the vbs thru' Java
'****************************************************

Set objArgs = WScript.Arguments

strOverAllSummaryFileAttachment=objArgs.item(0)  'File to be attached
arrFileName=SPLIT(REPLACE(strOverAllSummaryFileAttachment,"\\","\"),"\")

'1.b Get the current directory of vbs
'*********************************
oShell.CurrentDirectory = gobjFso.GetParentFolderName(Wscript.ScriptFullName)
gstrCurrentDirectory=CStr(oShell.CurrentDirectory)

'1.c. Read the INI File
'*****************
Call ReadQCDataINI(gstrCurrentDirectory)


'2.	Connect to QC, if the QC integration is in place.
'*********************************************************
'Can be changed

Set tdc = CreateObject("tdapiole80.tdconnection")
if NOT(tdc.Connected) then

	tdc.InitConnectionEx gstrServerURL
	tdc.Login gstrUserName, gstrPassword
	tdc.Connect gstrDomainName, gstrProjectName
	
End If

WScript.Echo "SENDING EMAIL. PLEASE WAIT...."
'3. Get info on the current test set 
'************************************
Set TestSetFact = tdc.TestSetFactory
Set tsTreeMgr = tdc.TestSetTreeManager
Set tSetFolder = tsTreeMgr.NodeByPath(gstrTestSetFolder)
Set TestSetsList = tSetFolder.FindTestSets(gstrTestSetName)
Set theTestSet = TestSetsList.Item(1)
Set TSTestFact = theTestSet.TSTestFactory
Set TSTestFilter=TSTestFact.Filter

'Test Cases in test set
Set TestSetTestsList = TSTestFact.NewList("")

'Add attachment to the current test set
'**************************************
Set objAttachments=theTestSet.Attachments
Set objAttachment=objAttachments.AddItem(Null)
objAttachment.FileName =TRIM(strOverAllSummaryFileAttachment)
objAttachment.Type=1
objAttachment.Post	
objAttachment.Refresh
			
Dim AttachmentPath(0)
Set AttachList = objAttachments.NewList("")
For i = 1 to AttachList.count
If AttachList.item(i).Name(1) = arrFileName(UBOUND(arrFileName))then
AttachmentPath(0) = AttachList.item(i).ServerFileName
Exit For
End If
Next

'Get the over all pass fail count of the test set to be attached to the mail
'******************************************************************
intPassCount=0
intFailCount=0
intNCCount=0
intNRCount=0
intBlockedCount=0
For Each theTSTest In TestSetTestsList
        strTCName=theTSTest.Name
		strTCStatus=theTSTest.Status

		If  UCASE(TRIM(strTCStatus))="PASSED" Then
			intPassCount=intPassCount+1
		ElseIf UCASE(TRIM(strTCStatus))="FAILED" Then
			intFailCount=intFailCount+1
		ElseIf UCASE(TRIM(strTCStatus))="NO RUN" Then
			intNRCount=intNRCount+1	
		ElseIf UCASE(TRIM(strTCStatus))="NOT COMPLETED" Then
			intNCCount=intNCCount+1
		ElseIf UCASE(TRIM(strTCStatus))="BLOCKED" Then
			intNCCount=intNCCount+1
		End If
Next 

strDateNow=Date()
strTimeNow=Time()
arrCurrentRelease=SPLIT(gstrTestSetFolder,"\")
intTotalTestCases=intPassCount+intFailCount+intNRCount+intNCCount+intBlockedCount
strContent1="Hi,<BR/>"
strContent2="<BR/>Please find below the Over All Automated Regression Execution Status as of - " & strDateNow &" " & strTimeNow & ". Kindly refer the attached excel for the latest run results.<BR/>"
strContent3="<BR/><B>ALM DETAILS</B><BR/><B>**************</B><BR/><BR/><B>1. Release Name   : </B>" & SPLIT(arrCurrentRelease(2),"-")(1) & "<BR/><B>2. Test Set Folder : </B>" & UCASE(gstrTestSetFolder) & "<BR/><B>3. Test Set Name  : </B>" & UCASE(gstrTestSetName) & "<BR/>"
strHeading1=  "<BR/><B>OVER ALL STATUS EXECUTION STATUS<B><BR/>"
strHeading1a= "<B>*****************************************<B><BR/>"
strFinalStatus="<table border=1> <tr> <th bgcolor=#82CAFF><b><center> TOTAL NO. TEST CASES</center></b></th><th bgcolor=#82CAFF><b><center> PASS </center></b></th> <th bgcolor=#82CAFF><b><center> FAIL </center></b><th bgcolor=#82CAFF><b><center>BLOCKED</center></b></th></th> <th bgcolor=#82CAFF><b><center>NOT COMPLETED</center></b></th> <th bgcolor=#82CAFF><b><center>NO RUN</center></b></th> </tr><tr> <td> <b><center>" & intTotalTestCases &"</center></b></td><td> <b><center><Font color=green>" & intPassCount &"</font></center></b></td> <td> <b><center><font color=red>" & intFailCount &"</font></center></b></td><td> <b><center>" & intBlockedCount &"</center></b></td><td> <b><center>" & intNCCount &"</center></b></td><td> <b><center>" & intNRCount &"</center></b></td> </tr> </table>"
strSignature1="<BR/><BR/>Thanks and Regards,"
strSignature2="<BR/><BR/> HP ALM AUTO GENERATED EMAIL. PLEASE CONTACT THE SENDER FOR FURTHER QUERIES."
strMailSubject="OVERALL ALM STATUS_AUTOMATED REGRESSION_" & UCASE(SPLIT(arrCurrentRelease(2),"-")(1)) & "_" & UCASE(arrCurrentRelease(3)) & "_" & strDateNow &" " & strTimeNow
strMailContent=strContent1 & strContent2 & strContent3 & strHeading1 & strHeading1a & strFinalStatus & strSignature1 & strSignature2
 
tdc.SendMail gstrEmailID, "", strMailSubject, strMailContent, AttachmentPath, "HTML"	
	
		
'Release all objects
'*******************

tdc.Logout 
tdc.Disconnect 
Set tdc=Nothing
Set objWorkSheet = Nothing
Set objWorkBook = Nothing
Set objExcel = Nothing
Set gobjFso=Nothing
Set TestSetFact = Nothing
Set tsTreeMgr = Nothing
Set tSetFolder = Nothing
Set TestSetsList = Nothing
Set theTestSet = Nothing
Set TSTestFact = Nothing
Set TSTestFilter=Nothing
Set objRunFactory=Nothing
Set objThisRun=Nothing


'********************************************************************************************************************************************************************
'#####################################################################################################################
'Function Name    		: ReadQCDataINI
'Description     		: Function to read the QC data ini file. User can use the same variable specified in the file
'Input Parameters 		: Current Directory
'Return Value    		: Nothing
'Author				    : S. Ramgopal Narayanan
'Date Created			: 14/3/11
'#####################################################################################################################
Function ReadQCDataINI(CurrentDirectory)
	Dim arrFileLines(),strQCDataPath
	intCntr = 0
	Set objFSO = CreateObject("Scripting.FileSystemObject")
	strQCDataPath = CurrentDirectory & "\ALMDATA.ini"
	Set objFile = objFSO.OpenTextFile(strQCDataPath, 1)

	Do Until objFile.AtEndOfStream
		 Redim Preserve arrFileLines(intCntr)
		 arrFileLines(intCntr) = objFile.ReadLine
		 intCntr = intCntr + 1
	Loop
	objFile.Close
	For intCount = Ubound(arrFileLines) to 1  Step -1
		If arrFileLines(intCount)<>"" Then 
			strTemp=split (arrFileLines(intCount),"=")
			strVar= strTemp(0)
			strVal=strTemp(1)  
			Execute strVar & " = strVal"
		End If
	Next
	Set objFSO=Nothing
End Function
'#####################################################################################################################
