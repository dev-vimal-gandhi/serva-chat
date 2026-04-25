package com.servalabs.chat.jobmanager.impl;

import android.app.Application;
import android.app.job.JobInfo;

import androidx.annotation.NonNull;

import com.servalabs.chat.jobmanager.Constraint;
import com.servalabs.chat.service.KeyCachingService;

public class MasterSecretConstraint implements Constraint {

  public static final String KEY = "MasterSecretConstraint";

  @NonNull
  @Override
  public String getFactoryKey() {
    return KEY;
  }

  @Override
  public boolean isMet() {
    return !KeyCachingService.isLocked();
  }

  @Override
  public void applyToJobInfo(@NonNull JobInfo.Builder jobInfoBuilder) {}

  public static final class Factory implements Constraint.Factory<MasterSecretConstraint> {

    public Factory(@NonNull Application application) {}

    @Override
    public MasterSecretConstraint create() {
      return new MasterSecretConstraint();
    }
  }
}
