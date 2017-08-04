package com.chris

class InvalidTokenException (message: String?): IllegalArgumentException(message)
class NotFoundException (message: String?): Exception(message)