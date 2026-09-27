package com.example.util

/**
 * Reserved boundary for a future place-search provider.
 *
 * The core build uses curated map destinations and does not make an implicit
 * Android Geocoder/network request. Keeping this inert declaration also prevents
 * older AI Studio overlay imports from restoring the former active implementation.
 */
internal object LocationGeocoderDisabled
