package com.example.worknetregister

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Patterns
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    lateinit var nama: EditText
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var confirmPassword: EditText
    lateinit var radioGender: RadioGroup
    lateinit var spinnerProvinsi: Spinner
    lateinit var spinnerKota: Spinner
    lateinit var btnRegister: MaterialButton

    lateinit var layoutPassword: TextInputLayout
    lateinit var layoutConfirmPassword: TextInputLayout

    lateinit var cbMusik: CheckBox
    lateinit var cbOlahraga: CheckBox
    lateinit var cbMembaca: CheckBox
    lateinit var cbGaming: CheckBox

    val provinsiList = ArrayList<String>()
    val provinsiIdList = ArrayList<String>()
    val kotaList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val termsText = findViewById<TextView>(R.id.txtTerms)
        val text = getString(R.string.terms_text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            termsText.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            termsText.text = Html.fromHtml(text)
        }

        nama = findViewById(R.id.etNama)
        email = findViewById(R.id.etEmail)
        password = findViewById(R.id.etPassword)
        confirmPassword = findViewById(R.id.etConfirmPassword)
        radioGender = findViewById(R.id.radioGender)
        spinnerProvinsi = findViewById(R.id.spinnerProvinsi)
        spinnerKota = findViewById(R.id.spinnerKota)
        btnRegister = findViewById(R.id.btnRegister)

        layoutPassword = findViewById(R.id.layoutPassword)
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword)

        cbMusik = findViewById(R.id.cbMusik)
        cbOlahraga = findViewById(R.id.cbOlahraga)
        cbMembaca = findViewById(R.id.cbMembaca)
        cbGaming = findViewById(R.id.cbGaming)

        kotaList.add("Pilih Kota")
        spinnerKota.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kotaList)

        loadProvinsi()

        spinnerProvinsi.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    if (position > 0) {
                        val provId = provinsiIdList[position]
                        loadKota(provId)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        password.addTextChangedListener(passwordWatcher)
        confirmPassword.addTextChangedListener(passwordWatcher)

        val scaleAnimation = ScaleAnimation(
            1f, 1.05f,
            1f, 1.05f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = 800
        scaleAnimation.repeatMode = Animation.REVERSE
        scaleAnimation.repeatCount = Animation.INFINITE
        btnRegister.startAnimation(scaleAnimation)

        btnRegister.setOnLongClickListener {
            validasiForm()
            true
        }
    }

    private fun loadProvinsi() {
        val url = "https://www.emsifa.com/api-wilayah-indonesia/api/provinces.json"
        val queue = Volley.newRequestQueue(this)

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                provinsiList.clear()
                provinsiIdList.clear()

                provinsiList.add("Pilih Provinsi")
                provinsiIdList.add("0")

                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    provinsiList.add(obj.getString("name"))
                    provinsiIdList.add(obj.getString("id"))
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, provinsiList)
                spinnerProvinsi.adapter = adapter
            },
            {
                Toast.makeText(this, "Gagal load provinsi", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(request)
    }

    private fun loadKota(provinceId: String) {
        val url = "https://www.emsifa.com/api-wilayah-indonesia/api/regencies/$provinceId.json"
        val queue = Volley.newRequestQueue(this)

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response: JSONArray ->
                kotaList.clear()
                kotaList.add("Pilih Kota")

                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    kotaList.add(obj.getString("name"))
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kotaList)
                spinnerKota.adapter = adapter
            },
            {
                Toast.makeText(this, "Gagal load kota", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }

    private val passwordWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            checkPasswordMatch()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun checkPasswordMatch() {
        val pass = password.text.toString()
        val confirm = confirmPassword.text.toString()

        if (pass.length < 6) {
            layoutPassword.error = "Minimal 6 karakter"
            layoutPassword.boxStrokeColor = getColor(android.R.color.holo_red_dark)
        } else {
            layoutPassword.error = null
            layoutPassword.boxStrokeColor = getColor(android.R.color.holo_green_dark)
        }

        if (confirm.isNotEmpty()) {
            if (pass != confirm) {
                layoutConfirmPassword.error = "Password tidak sesuai"
                layoutConfirmPassword.boxStrokeColor = getColor(android.R.color.holo_red_dark)
            } else {
                layoutConfirmPassword.error = null
                layoutConfirmPassword.boxStrokeColor = getColor(android.R.color.holo_green_dark)
            }
        }
    }

    private fun showAlert(pesan: String) {
        AlertDialog.Builder(this)
            .setTitle("Notifikasi")
            .setMessage(pesan)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun validasiForm() {

        // 1. Nama
        if (nama.text.toString().isEmpty()) {
            showAlert("Nama harus diisi")
            return
        }

        // 2. Email
        if (email.text.toString().isEmpty()) {
            showAlert("Email harus diisi")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            showAlert("Email tidak valid")
            return
        }

        // 3. Password
        if (password.text.toString().isEmpty()) {
            showAlert("Password harus diisi")
            return
        }

        if (password.text.toString().length < 6) {
            showAlert("Password minimal 6 karakter")
            return
        }

        // 4. Jenis Kelamin
        if (radioGender.checkedRadioButtonId == -1) {
            showAlert("Pilih jenis kelamin terlebih dahulu")
            return
        }

        // 5. Hobi
        var count = 0
        if (cbMusik.isChecked) count++
        if (cbOlahraga.isChecked) count++
        if (cbMembaca.isChecked) count++
        if (cbGaming.isChecked) count++

        if (count < 3) {
            showAlert("Pilih minimal 3 hobi")
            return
        }

        // 6. Provinsi
        if (spinnerProvinsi.selectedItemPosition == 0) {
            showAlert("Pilih provinsi terlebih dahulu")
            return
        }

        // 7. Kota
        if (spinnerKota.selectedItemPosition == 0) {
            showAlert("Pilih kota terlebih dahulu")
            return
        }

        // 8. Confirm Password
        if (confirmPassword.text.toString().isEmpty()) {
            showAlert("Confirm Password harus diisi")
            return
        }

        if (password.text.toString() != confirmPassword.text.toString()) {
            showAlert("Password tidak sama")
            return
        }

        // 9. Konfirmasi
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah data sudah benar?")
            .setPositiveButton("Ya") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Berhasil")
                    .setMessage("Akun berhasil dibuat")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}