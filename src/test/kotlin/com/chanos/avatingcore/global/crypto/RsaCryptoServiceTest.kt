package com.chanos.avatingcore.global.crypto

import com.chanos.avatingcore.crypto.service.RsaCryptoService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

class RsaCryptoServiceTest : BehaviorSpec({

    val ALGORITHM = "RSA"
    val CIPHER_SPEC = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    val MAX_ENCRYPTABLE_BYTES = 190

    val keyPair = KeyPairGenerator.getInstance(ALGORITHM)
        .apply { initialize(2048) }
        .generateKeyPair()
    val rsaCryptoService = RsaCryptoService(keyPair)

    fun encrypt(plainText: String, publicKeyBase64: String = rsaCryptoService.getPublicKeyBase64()): String {
        val decoded = Base64.getDecoder().decode(publicKeyBase64)
        val publicKey = KeyFactory.getInstance(ALGORITHM).generatePublic(X509EncodedKeySpec(decoded))
        val cipher = Cipher.getInstance(CIPHER_SPEC)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.toByteArray(Charsets.UTF_8)))
    }

    given("공개키 조회 요청이 주어졌을 때") {
        `when`("getPublicKeyBase64를 호출하면") {
            val result = rsaCryptoService.getPublicKeyBase64()

            then("Base64로 인코딩된 공개키를 반환한다") {
                result.shouldNotBeBlank()
            }
        }
    }

    given("서비스 공개키로 암호화한 영문 평문이 주어졌을 때") {
        val original = "Passw0rd!"

        `when`("decrypt를 호출하면") {
            val result = rsaCryptoService.decrypt(encrypt(original))

            then("원문과 일치한다") {
                result shouldBe original
            }
        }
    }

    given("서비스 공개키로 암호화한 한글 포함 평문이 주어졌을 때") {
        val original = "한글비밀번호1!"

        `when`("decrypt를 호출하면") {
            val result = rsaCryptoService.decrypt(encrypt(original))

            then("원문과 일치한다") {
                result shouldBe original
            }
        }
    }

    given("서비스 공개키로 암호화한 빈 문자열이 주어졌을 때") {
        val original = ""

        `when`("decrypt를 호출하면") {
            val result = rsaCryptoService.decrypt(encrypt(original))

            then("빈 문자열을 반환한다") {
                result shouldBe original
            }
        }
    }

    given("서비스 공개키로 암호화한 190바이트 경계값 평문이 주어졌을 때") {
        val original = "a".repeat(MAX_ENCRYPTABLE_BYTES)

        `when`("decrypt를 호출하면") {
            val result = rsaCryptoService.decrypt(encrypt(original))

            then("원문과 일치한다") {
                result shouldBe original
            }
        }
    }

    given("잘못된 Base64 입력이 주어졌을 때") {
        `when`("decrypt를 호출하면") {
            then("예외가 발생한다") {
                shouldThrow<Exception> {
                    rsaCryptoService.decrypt("not-valid-base64!!!")
                }
            }
        }
    }

    given("다른 키 쌍의 공개키로 암호화한 데이터가 주어졌을 때") {
        val otherKeyPair = KeyPairGenerator.getInstance(ALGORITHM)
            .apply { initialize(2048) }
            .generateKeyPair()
        val otherPublicKeyBase64 = Base64.getEncoder().encodeToString(otherKeyPair.public.encoded)

        `when`("decrypt를 호출하면") {
            then("예외가 발생한다") {
                shouldThrow<Exception> {
                    rsaCryptoService.decrypt(encrypt("Passw0rd!", otherPublicKeyBase64))
                }
            }
        }
    }

    given("암호화되지 않은 평문 Base64가 주어졌을 때") {
        val fakeEncrypted = Base64.getEncoder().encodeToString("plain-text-not-encrypted".toByteArray())

        `when`("decrypt를 호출하면") {
            then("예외가 발생한다") {
                shouldThrow<Exception> {
                    rsaCryptoService.decrypt(fakeEncrypted)
                }
            }
        }
    }

    given("190바이트를 초과하는 평문이 주어졌을 때") {
        val tooLong = "가".repeat(64) // UTF-8 기준 192 bytes

        `when`("encrypt를 호출하면") {
            then("RSA 한계 초과로 예외가 발생한다") {
                tooLong.toByteArray(Charsets.UTF_8).size shouldBeGreaterThan MAX_ENCRYPTABLE_BYTES
                shouldThrow<Exception> { encrypt(tooLong) }
            }
        }
    }
})
