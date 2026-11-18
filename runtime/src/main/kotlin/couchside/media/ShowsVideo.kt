package couchside.media

/** A view whose picture is a video surface (a player view): AndroidView draws its [videoOutput] in Compose. */
interface ShowsVideo {
    val videoOutput: VideoOutput
    val videoFit: Fit
}
