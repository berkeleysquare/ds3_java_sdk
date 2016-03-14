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

// This code is auto-generated, do not modify
package com.spectralogic.ds3client.commands.spectrads3.notifications;

import com.spectralogic.ds3client.commands.notifications.AbstractGetNotificationRequest;
import java.util.UUID;

public class GetStorageDomainFailureNotificationRegistrationsSpectraS3Request extends AbstractGetNotificationRequest {

    // Variables
    
    private boolean lastPage;

    private int pageLength;

    private int pageOffset;

    private UUID pageStartMarker;

    private UUID userId;

    // Constructor
    
    public GetStorageDomainFailureNotificationRegistrationsSpectraS3Request(final UUID notificationId) {
        super(notificationId);

        
    }

    public GetStorageDomainFailureNotificationRegistrationsSpectraS3Request withLastPage(final boolean lastPage) {
        this.lastPage = lastPage;
        if (this.lastPage) {
            this.getQueryParams().put("last_page", null);
        } else {
            this.getQueryParams().remove("last_page");
        }
        return this;
    }

    public GetStorageDomainFailureNotificationRegistrationsSpectraS3Request withPageLength(final int pageLength) {
        this.pageLength = pageLength;
        this.updateQueryParam("page_length", pageLength);
        return this;
    }

    public GetStorageDomainFailureNotificationRegistrationsSpectraS3Request withPageOffset(final int pageOffset) {
        this.pageOffset = pageOffset;
        this.updateQueryParam("page_offset", pageOffset);
        return this;
    }

    public GetStorageDomainFailureNotificationRegistrationsSpectraS3Request withPageStartMarker(final UUID pageStartMarker) {
        this.pageStartMarker = pageStartMarker;
        this.updateQueryParam("page_start_marker", pageStartMarker);
        return this;
    }

    public GetStorageDomainFailureNotificationRegistrationsSpectraS3Request withUserId(final UUID userId) {
        this.userId = userId;
        this.updateQueryParam("user_id", userId);
        return this;
    }


    @Override
    public String getPath() {
        return "/_rest_/storage_domain_failure_notification_registration/" + this.getNotificationId().toString();
    }

    
    public boolean getLastPage() {
        return this.lastPage;
    }


    public int getPageLength() {
        return this.pageLength;
    }


    public int getPageOffset() {
        return this.pageOffset;
    }


    public UUID getPageStartMarker() {
        return this.pageStartMarker;
    }


    public UUID getUserId() {
        return this.userId;
    }

}