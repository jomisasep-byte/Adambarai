package com.example

import android.app.Application
import com.example.data.local.MusicGuardDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.repository.MusicGuardRepository
import com.example.media.MediaInspector
import com.example.recognition.AuddRecognitionProvider
import com.example.recognition.RecognitionProvider

class MusicGuardApplication : Application() {
  val database: MusicGuardDatabase by lazy { MusicGuardDatabase.getInstance(this) }
  val repository: MusicGuardRepository by lazy { MusicGuardRepository(database.musicGuardDao()) }
  val settingsDataStore: SettingsDataStore by lazy { SettingsDataStore(this) }
  val mediaInspector: MediaInspector by lazy { MediaInspector(this) }
  val recognitionProvider: RecognitionProvider by lazy { AuddRecognitionProvider() }
}
