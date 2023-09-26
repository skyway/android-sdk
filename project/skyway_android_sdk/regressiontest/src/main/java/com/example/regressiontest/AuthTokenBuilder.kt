package com.example.regressiontest

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64;

object AuthTokenBuilder {
    @JvmStatic
    fun CreateToken(app_id: String, secret_key: String):String{
        val jti = UUID.randomUUID().toString()
        val iat = System.currentTimeMillis()/1000
        val exp = iat + 6000
        val token = """
        {
            jti: ${jti},
            iat: ${iat},
            exp: ${exp},
            scope: {
                app: {
                    id: "${app_id}",
                    turn: true,
                    actions: ["read"],
                    channels: [
                        {
                            id: "*",
                            name: "*",
                            actions: ["write"],
                            members: [
                                {
                                    id: "*",
                                    name: "*",
                                    actions: ["write"],
                                    publication: { 
                                        actions: ["write"]
                                    },
                                    subscription: { 
                                        actions: ["write"]
                                    }
                                }
                            ],
                            sfuBots: [
                                {
                                    actions: ["write"],
                                    forwardings: [
                                        {
                                            actions: ["write"]
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            }
        }
        """.trimIndent().trimEnd()
        val temp = AuthTokenBuilder.Build(secret_key,Gson().fromJson(token,JsonObject::class.java))
        return temp
    }

    @JvmStatic
    private fun Build(secret_key: String,token: JsonObject):String{
        return BuildJWT(secret_key,token)
    }

    @JvmStatic
    private fun EncodeBase64Url(tokenByte: ByteArray):String{
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenByte)
    }

    @JvmStatic
    private fun BuildSHA256(secret_key: String,token: String):ByteArray{
        val algorithm = "HmacSHA256"
        val keySpec = SecretKeySpec(secret_key.toByteArray(Charsets.UTF_8), algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(keySpec)
        val sign = mac.doFinal(token.toByteArray(Charsets.UTF_8))

        return sign
    }
    @JvmStatic
    private fun BuildJWT(secret_key: String,header:JsonObject, payload:JsonObject):String{
        var token = ""

        token+= EncodeBase64Url(Gson().toJson(header).toByteArray())
        token += "."
        token += EncodeBase64Url(Gson().toJson(payload).toByteArray())
        val verify = EncodeBase64Url(BuildSHA256(secret_key, token))
        token += "."
        token += verify

        return token
    }
    @JvmStatic
    private fun BuildJWT(secret_key: String,payload:JsonObject):String{
        return AuthTokenBuilder.BuildJWT(
            secret_key,
            Gson().fromJson(
                """
                {
                    alg: "HS256",
                    typ: "JWT"
                }
                """.trimIndent(), JsonObject::class.java),
            payload
        )
    }

}
