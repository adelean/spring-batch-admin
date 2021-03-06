/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.admin.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
@Ignore
public class JobExecutionTests {

	private JobParameters jobParameters = new JobParametersBuilder().addString("fail", "false").toJobParameters();

	@Autowired
	private JobService jobService;

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Test
	public void testSimpleProperties() throws Exception {
		assertNotNull(jobService);
	}

	@Test
	public void testLaunchJsrBasedJob() throws Exception {
		int before = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		JobExecution jobExecution = jobService.launch("jsr352-job", jobParameters);

		while(jobExecution.isRunning()) {
			jobExecution = jobService.getJobExecution(jobExecution.getId());
		}

		assertNotNull(jobExecution);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		int after = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		assertEquals(before + 1, after);
	}

	@Test
	public void testLaunchJavaConfiguredJob() throws Exception {
		int before = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		JobExecution jobExecution = jobService.launch("javaJob", jobParameters);
		assertNotNull(jobExecution);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		int after = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		assertEquals(before + 1, after);
	}

	@Test
	public void testLaunchJob() throws Exception {
		int before = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		JobExecution jobExecution = jobService.launch("job1", jobParameters);
		assertNotNull(jobExecution);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		int after = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		assertEquals(before + 1, after);
	}

	@Test
	public void testFailedJob() throws Exception {
		int before = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		jobParameters = new JobParametersBuilder().addString("fail", "true").toJobParameters();
		JobExecution jobExecution = jobService.launch("job1", jobParameters);
		assertNotNull(jobExecution);
		assertEquals(BatchStatus.FAILED, jobExecution.getStatus());
		int after = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		assertEquals(before + 1, after);
	}

	@Test
	public void testLaunchTwoJobs() throws Exception {
		int before = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		long count = 0;
		JobExecution jobExecution1 = jobService.launch("job1", new JobParametersBuilder(jobParameters).addLong("run.id", count++)
				.toJobParameters());
		JobExecution jobExecution2 = jobService.launch("job1", new JobParametersBuilder(jobParameters).addLong("run.id", count++)
				.toJobParameters());
		assertEquals(BatchStatus.COMPLETED, jobExecution1.getStatus());
		assertEquals(BatchStatus.COMPLETED, jobExecution2.getStatus());
		int after = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STEP_EXECUTION");
		assertEquals(before + 2, after);
	}

}
