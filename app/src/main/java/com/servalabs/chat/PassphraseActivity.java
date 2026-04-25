/**
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.servalabs.chat;

import android.content.Intent;

import com.servalabs.chat.core.util.logging.Log;
import com.servalabs.chat.crypto.MasterSecret;
import com.servalabs.chat.service.KeyCachingService;


/**
 * Base Activity for changing/prompting local encryption passphrase.
 *
 * @author Moxie Marlinspike
 */
public abstract class PassphraseActivity extends BaseActivity {

  private static final String TAG = Log.tag(PassphraseActivity.class);

  public void setMasterSecret(final MasterSecret masterSecret) {
    KeyCachingService.setMasterSecret(masterSecret);
    startService(new Intent(this, KeyCachingService.class));

    ApplicationContext.getInstance(this).onUnlock();
  }

  void launchRoutedActivity() {
    Intent nextIntent = getIntent().getParcelableExtra("next_intent");
    if (nextIntent != null) {
      try {
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(nextIntent);
      } catch (java.lang.SecurityException e) {
        Log.w(TAG, "Access permission not passed from PassphraseActivity, retry sharing.");
      }
      finish();
    }
  }
}
