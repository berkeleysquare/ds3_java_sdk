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

package com.spectralogic.ds3client.commands;

import com.spectralogic.ds3client.models.bulk.MasterObjectList;
import com.spectralogic.ds3client.networking.WebResponse;
import com.spectralogic.ds3client.serializer.XmlOutput;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public abstract class BulkResponse extends AbstractResponse {
    private final static Logger LOG = LoggerFactory.getLogger(BulkResponse.class);
    private MasterObjectList result;
    public BulkResponse(final WebResponse response) throws IOException {
        super(response);
    }

    public MasterObjectList getResult() {
        return this.result;
    }

    @Override
    protected void processResponse() throws IOException {
        try (final WebResponse response = this.getResponse()) {
            this.checkStatusCode(200);
            try(final InputStream content = response.getResponseStream()) {
                LOG.debug("Starting bulk response parsing");
                this.result = XmlOutput.fromXml(content, MasterObjectList.class);
                LOG.debug("Finished bulk response parsing");
            }
        }
    }
}