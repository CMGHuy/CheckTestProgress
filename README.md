# CheckTestProgress
After collecting subreport_text files from worker instances, this application is used to generate the summarized information. 
Firstly, the simplified information is extracted from each subreport_text file.  
Then, the summation of those information are exported.

The old files in subreport_text and currentProgress folder are cleaned up for every run.  
It ensures that the files are always on new state, not be appended.  