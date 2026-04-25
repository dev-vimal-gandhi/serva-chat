package com.servalabs.chat.jobs;

import androidx.annotation.NonNull;

import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.jobmanager.impl.MasterSecretConstraint;

abstract class MasterSecretJob extends Job {
  MasterSecretJob(@NonNull Parameters parameters) {
    super(addMasterSecretConstraint(parameters));
  }

  private static Parameters addMasterSecretConstraint(Parameters parameters) {
    return parameters.toBuilder().addConstraint(MasterSecretConstraint.KEY).build();
  }
}