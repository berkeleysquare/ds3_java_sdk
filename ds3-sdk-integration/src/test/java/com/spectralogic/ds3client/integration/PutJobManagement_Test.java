/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

package com.spectralogic.ds3client.integration;

import com.google.common.collect.Lists;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.*;
import com.spectralogic.ds3client.commands.spectrads3.*;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;
import com.spectralogic.ds3client.helpers.options.WriteJobOptions;
import com.spectralogic.ds3client.integration.test.helpers.TempStorageIds;
import com.spectralogic.ds3client.integration.test.helpers.TempStorageUtil;
import com.spectralogic.ds3client.models.*;
import com.spectralogic.ds3client.models.bulk.Ds3Object;
import com.spectralogic.ds3client.serializer.XmlProcessingException;
import com.spectralogic.ds3client.utils.ResourceUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.spectralogic.ds3client.helpers.Ds3ClientHelpers.*;
import static com.spectralogic.ds3client.integration.Util.*;
import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.Matcher;
import static org.junit.Assert.*;

public class PutJobManagement_Test {

    private static Ds3Client client;
    private static final String TEST_ENV_NAME = "PutJobManagement_Test";
    private static TempStorageIds envStorageIds;

    @BeforeClass
    public static void startup() throws IOException, SignatureException {
        client = Util.fromEnv();
        final UUID dataPolicyId = TempStorageUtil.setupDataPolicy(TEST_ENV_NAME, true, ChecksumType.Type.MD5, client);
        envStorageIds = TempStorageUtil.setup(TEST_ENV_NAME, dataPolicyId, client);
    }

    @AfterClass
    public static void teardown() throws IOException, SignatureException {
        TempStorageUtil.teardown(TEST_ENV_NAME, envStorageIds, client);
        client.close();
    }
    private void checkTimeOut(long startTime, int testTimeOutSeconds){
        assertTrue((System.nanoTime() - startTime)/1000000000 <= testTimeOutSeconds);
        //assertThat((System.nanoTime() - startTime)/1000000000, is(lessThan(testTimeOutSeconds));
    }

    @Test
    public void modifyJobPriority() throws IOException, SignatureException, XmlProcessingException {
        final String bucketName = "test_modify_job_priority";
        try {
            Ds3ClientHelpers.wrap(client).ensureBucketExists(bucketName);
            
            final List<Ds3Object> objects = Lists.newArrayList( new Ds3Object("test", 2));

            final WriteJobOptions jobOptions = WriteJobOptions.create().withPriority(Priority.LOW);

            final Ds3ClientHelpers.Job job =
                    wrap(client).startWriteJob(bucketName, objects, jobOptions);

            client.modifyJobSpectraS3(new ModifyJobSpectraS3Request(job.getJobId())
                    .withPriority(Priority.HIGH));

            final GetJobSpectraS3Response response = client
                    .getJobSpectraS3(new GetJobSpectraS3Request(job.getJobId()));

            assertThat(response.getMasterObjectListResult().getPriority(), is(Priority.HIGH));

        } finally {
            deleteAllContents(client, bucketName);
        }
    }

    @Test
    public void modifyJobName() throws IOException, SignatureException, XmlProcessingException {
        final String bucketName = "test_modify_job_name";
        try {
            Ds3ClientHelpers.wrap(client).ensureBucketExists(bucketName);
            
            final List<Ds3Object> objects = Lists.newArrayList(new Ds3Object("test",2));

            final Ds3ClientHelpers.Job job =
                    wrap(client).startWriteJob(bucketName, objects);

            client.modifyJobSpectraS3(new ModifyJobSpectraS3Request(job.getJobId())
                    .withName("newName"));

            final GetJobSpectraS3Response response = client
                    .getJobSpectraS3(new GetJobSpectraS3Request(job.getJobId()));

            assertThat(response.getMasterObjectListResult().getName(), is("newName"));

        } finally {
            deleteAllContents(client, bucketName);
        }
    }

    @Test
    public void modifyJobCreationDate() throws IOException, SignatureException, XmlProcessingException {
        final String bucketName = "test_modify_job_creation_date";
        try {
            Ds3ClientHelpers.wrap(client).ensureBucketExists(bucketName);

            final List<Ds3Object> objects = Lists.newArrayList(new Ds3Object("test", 2));

            final Ds3ClientHelpers.Job job =
                    wrap(client).startWriteJob(bucketName, objects);
            final GetJobSpectraS3Response jobResponse = client
                    .getJobSpectraS3(new GetJobSpectraS3Request(job.getJobId()));

            final Date originalDate = jobResponse.getMasterObjectListResult().getStartDate();
            final Date newDate = new Date(originalDate.getTime() - 1000);

            client.modifyJobSpectraS3(new ModifyJobSpectraS3Request(job.getJobId())
                    .withCreatedAt(newDate));

            final GetJobSpectraS3Response responseAfterModify = client
                    .getJobSpectraS3(new GetJobSpectraS3Request(job.getJobId()));

            assertThat(responseAfterModify.getMasterObjectListResult().getStartDate(), is(newDate));

        } finally {
            deleteAllContents(client, bucketName);
        }
    }

    @Test
    public void cancelJob() throws IOException, SignatureException, XmlProcessingException {
        final String bucketName = "test_cancel_job";
        try {
            Ds3ClientHelpers.wrap(client).ensureBucketExists(bucketName);

            final List<Ds3Object> objectsOne = Lists.newArrayList(new Ds3Object("testOne", 2));

            final Ds3ClientHelpers.Job job =
                    wrap(client).startWriteJob(bucketName, objectsOne);

            final CancelJobSpectraS3Response response = client
                    .cancelJobSpectraS3(new CancelJobSpectraS3Request(job.getJobId()));
            assertEquals(response.getStatusCode(),204);

            assertTrue(client.getActiveJobsSpectraS3(new GetActiveJobsSpectraS3Request())
                    .getActiveJobListResult().getActiveJobs().isEmpty());

        } finally {
            deleteAllContents(client, bucketName);
        }
    }

    @Test
    public void truncateJobCancelWithOutForce() throws IOException, SignatureException, XmlProcessingException, URISyntaxException, InterruptedException {

        final int testTimeOutSeconds = 5;
        final String bucketName = "test_truncate_cancel_job";
        final String book1 = "beowulf.txt";
        final Path objPath1 = ResourceUtils.loadFileResource(RESOURCE_BASE_NAME + book1);
        final Ds3Object obj1 = new Ds3Object(book1, Files.size(objPath1));
        final Ds3Object obj2 = new Ds3Object("place_holder", 5000000);

        try {
            Ds3ClientHelpers.wrap(client).ensureBucketExists(bucketName);
            Ds3ClientHelpers.wrap(client).ensureBucketExists(bucketName);

            final Ds3ClientHelpers.Job putJob = Ds3ClientHelpers.wrap(client).startWriteJob(bucketName, Lists.newArrayList(obj1, obj2));
            final UUID jobId = putJob.getJobId();
            final SeekableByteChannel book1Channel = new ResourceObjectPutter(RESOURCE_BASE_NAME).buildChannel(book1);
            client.putObject(new PutObjectRequest(bucketName, book1, book1Channel, jobId, 0, Files.size(objPath1)));

            //make sure black pearl has updated it's job to show 1 object in cache
            final long startTime = System.nanoTime();
            long cachedSize = 0;
            while (cachedSize == 0) {
                Thread.sleep(500);
                final MasterObjectList mol = client.getJobSpectraS3(new GetJobSpectraS3Request(jobId)).getMasterObjectListResult();
                cachedSize = mol.getCachedSizeInBytes();
                checkTimeOut(startTime, testTimeOutSeconds);
            }

            final CancelJobSpectraS3Response failedResponse = client.cancelJobSpectraS3(new CancelJobSpectraS3Request(jobId));
            assertThat(failedResponse.getStatusCode(),is(400));

            final GetJobSpectraS3Response truncatedJob = client.getJobSpectraS3(new GetJobSpectraS3Request(jobId));
            assertEquals(truncatedJob.getMasterObjectListResult().getOriginalSizeInBytes(), Files.size(objPath1));

        } finally {
            deleteAllContents(client, bucketName);
        }
    }

    @Test
    public void cancelJobWithForce() throws IOException, SignatureException, XmlProcessingException, URISyntaxException, InterruptedException {

        final int testTimeOutSeconds = 5;
        final String bucketName = "test_force_cancel_job";
        final String book1 = "beowulf.txt";
        final Path objPath1 = ResourceUtils.loadFileResource(RESOURCE_BASE_NAME + book1);
        final Ds3Object obj1 = new Ds3Object(book1, Files.size(objPath1));
        final Ds3Object obj2 = new Ds3Object("place_holder", 5000000);

        try {
            Ds3ClientHelpers.wrap(client).ensureBucketExists(bucketName);
            Ds3ClientHelpers.wrap(client).ensureBucketExists(bucketName);

            final Ds3ClientHelpers.Job putJob = Ds3ClientHelpers.wrap(client).startWriteJob(bucketName, Lists.newArrayList(obj1, obj2));
            final UUID jobId = putJob.getJobId();
            final SeekableByteChannel book1Channel = new ResourceObjectPutter(RESOURCE_BASE_NAME).buildChannel(book1);
            client.putObject(new PutObjectRequest(bucketName, book1, book1Channel, jobId, 0, Files.size(objPath1)));

            //make sure black pearl has updated it's job to show 1 object in cache
            final long startTimePutObject = System.nanoTime();
            long cachedSize = 0;
            while (cachedSize == 0) {
                Thread.sleep(500);
                final MasterObjectList mol = client.getJobSpectraS3(new GetJobSpectraS3Request(jobId)).getMasterObjectListResult();
                cachedSize = mol.getCachedSizeInBytes();
               checkTimeOut(startTimePutObject, testTimeOutSeconds);
            }

            final CancelJobSpectraS3Response responseWithForce = client
                    .cancelJobSpectraS3(new CancelJobSpectraS3Request(jobId).withForce(true));
            assertEquals(responseWithForce.getStatusCode(), 204);

            //Allow for lag time before canceled job appears~1.5 seconds in unloaded system
            final long startTimeCanceledUpdate = System.nanoTime();
            boolean jobCanceled = false;
            while (!jobCanceled) {
                Thread.sleep(500);
                final GetCanceledJobsSpectraS3Response canceledJobs = client.getCanceledJobsSpectraS3(new GetCanceledJobsSpectraS3Request());
                for (final CanceledJob canceledJob : canceledJobs.getCanceledJobListResult().getCanceledJobs()){
                    if (canceledJob.getId().equals(jobId)){
                        jobCanceled = true;
                    }
                }
                checkTimeOut(startTimeCanceledUpdate, testTimeOutSeconds);
            }

        } finally {
            deleteAllContents(client, bucketName);
        }
    }

    @Test
    public void cancelAllJobs() throws IOException, SignatureException, XmlProcessingException {
        final String bucketName = "test_cancel_all_jobs";
        try {
            Ds3ClientHelpers.wrap(client).ensureBucketExists(bucketName);

            final List<Ds3Object> objectsOne = Lists.newArrayList(new Ds3Object("testOne", 2));

            wrap(client).startWriteJob(bucketName, objectsOne);

            final List<Ds3Object> objectsTwo = Lists.newArrayList(new Ds3Object("testTwo", 2));

            wrap(client).startWriteJob(bucketName, objectsTwo);

            final CancelAllJobsSpectraS3Response response = client
                    .cancelAllJobsSpectraS3(new CancelAllJobsSpectraS3Request());
            response.checkStatusCode(204);

            assertTrue(client.getActiveJobsSpectraS3(new GetActiveJobsSpectraS3Request())
                    .getActiveJobListResult().getActiveJobs().isEmpty());
        } finally {
            deleteAllContents(client, bucketName);
        }
    }

    //todo public void cancelAllJobs() without force and 1 object uploaded
    //todo public void cancelAllJobs() with force and 1 object uploaded

    @Test
        public void getCanceledJobs() throws IOException, SignatureException, XmlProcessingException {
            final String bucketName = "test_get_canceled_jobs";
            try {
                Ds3ClientHelpers.wrap(client).ensureBucketExists(bucketName);

                final List<Ds3Object> objectsOne = new ArrayList<>();
                final Ds3Object obj = new Ds3Object("testOne", 2);
                objectsOne.add(obj);

                final Ds3ClientHelpers.Job jobOne =
                        wrap(client).startWriteJob(bucketName, objectsOne);
                final UUID jobOneId = jobOne.getJobId();
                client.cancelJobSpectraS3(new CancelJobSpectraS3Request(jobOneId));

                final GetCanceledJobsSpectraS3Response getCanceledJobsResponse = client
                        .getCanceledJobsSpectraS3(new GetCanceledJobsSpectraS3Request());

                List<UUID> canceledJobsUUIDs = new ArrayList<>();
                for (final CanceledJob job : getCanceledJobsResponse.getCanceledJobListResult().getCanceledJobs()) {
                    canceledJobsUUIDs.add(job.getId());
                }
                assertTrue(canceledJobsUUIDs.contains(jobOneId));

            } finally {
                deleteAllContents(client, bucketName);
            }
    }
}
