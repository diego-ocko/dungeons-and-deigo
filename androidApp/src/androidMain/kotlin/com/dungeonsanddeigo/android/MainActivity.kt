package com.dungeonsanddeigo.android

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.widget.*
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.availableSheetModels
import com.dungeonsanddeigo.repository.AndroidCharacterRepository

class MainActivity : Activity() {
    private var imageBase64: String? = null
    private val pickImageRequest = 1

    private lateinit var repo: AndroidCharacterRepository
    private lateinit var rootLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = AndroidCharacterRepository(this)
        rootLayout = FrameLayout(this)
        setContentView(rootLayout)
        showListScreen()
    }

    private fun showListScreen() {
        rootLayout.removeAllViews()
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; padding = 32 }

        val title = TextView(this).apply { text = "Dungeons And Deigo"; textSize = 24f }
        layout.addView(title)

        // Character list
        val characters = repo.getAll()
        if (characters.isEmpty()) {
            layout.addView(TextView(this).apply { text = "No characters created yet."; textSize = 16f })
        } else {
            characters.forEach { c ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 8)
                    isClickable = true
                    setOnClickListener { showCharacterDetail(c) }
                }
                if (c.imageBase64 != null) {
                    val bytes = Base64.decode(c.imageBase64!!.substringAfter(","), Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    row.addView(ImageView(this).apply {
                        setImageBitmap(bitmap)
                        layoutParams = LinearLayout.LayoutParams(100, 100).apply { marginEnd = 16 }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    })
                }
                row.addView(TextView(this).apply {
                    text = "${c.name} (${c.sheetModel.name})"
                    textSize = 16f
                    gravity = Gravity.CENTER_VERTICAL
                })
                layout.addView(row)
            }
        }

        // Create button
        val createBtn = Button(this).apply { text = "Create a new Character" }
        val formLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE }
        createBtn.setOnClickListener { formLayout.visibility = View.VISIBLE }
        layout.addView(createBtn)

        val nameInput = EditText(this).apply { hint = "Character Name" }
        formLayout.addView(nameInput)

        val modelSpinner = Spinner(this)
        modelSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, availableSheetModels.map { it.name })
        formLayout.addView(modelSpinner)

        val imageBtn = Button(this).apply { text = "Choose Image" }
        imageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, pickImageRequest)
        }
        formLayout.addView(imageBtn)

        val submitBtn = Button(this).apply { text = "Submit" }
        submitBtn.setOnClickListener {
            val selectedModel = availableSheetModels[modelSpinner.selectedItemPosition]
            val character = Character(nameInput.text.toString(), selectedModel.name, imageBase64)
            repo.insert(character)
            imageBase64 = null
            showCharacterDetail(character)
        }
        formLayout.addView(submitBtn)

        layout.addView(formLayout)
        rootLayout.addView(layout)
    }

    private fun showCharacterDetail(character: Character) {
        rootLayout.removeAllViews()
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; padding = 32 }

        // Header
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 0, 0, 16) }
        if (character.imageBase64 != null) {
            val bytes = Base64.decode(character.imageBase64!!.substringAfter(","), Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            header.addView(ImageView(this).apply {
                setImageBitmap(bitmap)
                layoutParams = LinearLayout.LayoutParams(200, 200).apply { marginEnd = 24 }
                scaleType = ImageView.ScaleType.CENTER_CROP
            })
        }
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_VERTICAL }
        info.addView(TextView(this).apply { text = character.name; textSize = 24f })
        info.addView(TextView(this).apply { text = "Sheet: ${character.sheetModel.name}"; textSize = 16f })
        header.addView(info)
        layout.addView(header)

        // Back button
        val backBtn = Button(this).apply { text = "Back to selection" }
        backBtn.setOnClickListener { showListScreen() }
        layout.addView(backBtn)

        rootLayout.addView(layout)
    }

    @Deprecated("Use Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageRequest && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val bytes = inputStream.readBytes()
            inputStream.close()
            val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
            imageBase64 = "data:image/png;base64,$encoded"
        }
    }
}

private var LinearLayout.padding: Int
    get() = paddingLeft
    set(value) = setPadding(value, value, value, value)
