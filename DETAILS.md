# OpenDataFlow


### The transactional model


Our approach is to treat each data set consumed or produced by a job as an indivisible unit, and one that is passed from output of one job to input of the next with the same kind of checks and controls that you would apply to any data transfer protocol.

Simply put, in order for a job to accept a chunk of data for consumption, it needs to confirm the answer "yes" to each of these five questions.

1. Is the data ready
2. Is it the correct data
3. Is the data in the physical location the consumer expects
4. Is the data validated
5. Is the data safe to access or modify

All data transfer protocols are built around these questions one way or another. It is quite reasonable to posit that ETL building on larger blocks of data have comparable requirements.  These are also the first questions the analyst must answer when tasked with finding root cause of job failures. 

Both the task of the analyst and the logic used by the consumer are simplified if this information is systematically captured at run time and directly used for coordination between jobs having data dependencies.

##  Details Definitions Requirements 


OpenDataFlow treats the data handoff between ETL processes in a transactional model. The handshake requires certain guarantees made by the producer in order that the consumer may accept the data. This is a data centric model in that these guarantees involve characteristics or status of the data being handed off. It is a departure from traditional job schedulers which are typically process centric instead. Where the traditional schedulers make assumptions about the data status, DataFlow instead specifically tracks the status and permits processes to rely on reliable data status.

Taking the perspective of an ETL resource, I want my script or process to be supplied all the input data (including all connection data), the location of the output data, any job sequence number, and to be guaranteed that I'm operating on the data sets required by business requirements, with no chance of gaps, overlaps, or undetected misses due to error states.  That keeps the ETL script simple as the most complex parts of the  logic is handled by the framework. It keeps the script simple and generic.
That requirement breaks down into the following:

1.  By partition we mean a chunk or slice of data that can serve as input or output for a particular job. 
Since we want to do concurrent runs, we assume that the data can be so partitioned that our jobs can operate independently on any data chunk and produce independent output.  For example it is common to partition by calendar date so that the batch cycle is a daily batch consuming data produced on previous day. 
2. capability to partition the data into chunks which can be independently processed, and the framework should associate a unique identifier to each chunk in order to track its status.  The undivided data set is called DataSet, the discrete partitions of it are called DataChunks.

3. Status of every data chunk is maintained and available to the ETL jobs. This status includes whether it is ready to consume, or still being produced, complete, or error or (sometimes important) being consumed.

4. Job Status. every job is associated with 1 or more data chunks input and produces one or more data chunks output. In practice, job status (running,failed,complete,validated) is the same as the status of its output data, so job status is denormalized into data status table. More specifically since every data set is uniquely produced as output of some job run, jobID (name of script for example) + dataID+datasetID serves to uniquely identify the job producing that data chunk.

5. transaction model. The data is to be transformed and forwarded downstream, the handling of it requires the kind of robustness that is usually supplied via transaction semantics.

6. Though not strictly necessary, jobs may require additional metadata, which can be constructed by the framework and supplied to them when they are started.


### Data model
The DataFlow core components manage three sets of information.

1. Real time status of all data sets tracked by the tool
2. Metadata for all tracked data sources
3. Metadata for fully registered jobs


