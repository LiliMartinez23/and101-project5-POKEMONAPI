package com.example.pokemonapi

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.squareup.picasso.Picasso
import kotlinx.coroutines.selects.SelectInstance
import okhttp3.Headers

class MainActivity : AppCompatActivity() {
    private lateinit var pbLoading: ProgressBar
    private lateinit var ivSprite: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvTypes: TextView
    private lateinit var tvInfo: TextView
    private lateinit var btnRandom: Button

    private val baseUrl = "https://pokeapi.co/api/v2/pokemon/"

    override fun onCreate( savedInstanceState: Bundle? ) {
        super.onCreate( savedInstanceState )
        setContentView( R.layout.activity_main )

        pbLoading = findViewById( R.id.pbLoading )
        ivSprite = findViewById( R.id.ivSprite )
        tvName = findViewById( R.id.tvName )
        tvTypes = findViewById( R.id.tvTypes )
        tvInfo = findViewById( R.id.tvInfo )
        btnRandom = findViewById( R.id.btnRandom )

        fetchPokemon("ditto")

        btnRandom.setOnClickListener {
            val randomId = ( 1..1025 ).random()
            fetchPokemon( randomId.toString() )
        }
    }

    private fun fetchPokemon( identifier: String ) {
        val url = baseUrl + identifier.lowercase()
        pbLoading.visibility = View.VISIBLE

        val client = AsyncHttpClient()
        client.get( url, object : JsonHttpResponseHandler() {
            override fun onSuccess( statusCode: Int, headers: Headers, json: JSON ) {
                pbLoading.visibility = View.GONE
                try {
                    val obj = json.jsonObject

                    val id = obj.getInt( "id" )
                    val name = obj.getString( "name" )
                    val height = obj.getInt( "height" )
                    val weight = obj.getInt( "weight" )

                    val sprites = obj.getJSONObject( "sprites" )
                    val spriteUrl = sprites.optString( "front_default", "" )

                    val typesArr = obj.getJSONArray( "types" )
                    val typeNames = mutableListOf<String>()
                    for ( i in 0 until typesArr.length() ) {
                        val typeName = typesArr.getJSONObject( i )
                            .getJSONObject( "type" )
                            .getString( "name" )
                            .replaceFirstChar { it.uppercase() }
                        typeNames.add( typeName )
                    }

                    tvName.text = "$name (#$id)"
                    tvTypes.text = "Type: ${typeNames.joinToString(", ")}"
                    tvInfo.text = "Height: $height Weight: $weight"

                    if ( spriteUrl.isNotEmpty() ) {
                        Picasso.get().load( spriteUrl ).into( ivSprite )
                    } else {
                        ivSprite.setImageDrawable( null )
                    }
                } catch ( e: Exception ) {
                    Toast.makeText( this@MainActivity, "Parse error", Toast.LENGTH_SHORT ).show()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                responsesString: String?,
                throwable: Throwable?
            ) {
                pbLoading.visibility = View.GONE
                Toast.makeText( this@MainActivity, "Request failed: $statusCode", Toast.LENGTH_SHORT ).show()
            }
        })
    }
}