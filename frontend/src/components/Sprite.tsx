import { useState } from 'react'

interface SpriteProps {
  src: string | null
  alt: string
  size: number
  pixelated?: boolean
  eager?: boolean
}

/** Image with fixed dimensions (no layout shift) and a neutral fallback when the sprite is missing. */
export function Sprite({ src, alt, size, pixelated = true, eager = false }: SpriteProps) {
  const [failed, setFailed] = useState(false)

  if (!src || failed) {
    return (
      <div
        role="img"
        aria-label={`${alt} (image unavailable)`}
        className="flex items-center justify-center rounded-full bg-slate-100 text-2xl text-slate-300"
        style={{ width: size, height: size }}
      >
        ?
      </div>
    )
  }
  return (
    <img
      src={src}
      alt={alt}
      width={size}
      height={size}
      loading={eager ? 'eager' : 'lazy'}
      decoding="async"
      onError={() => setFailed(true)}
      className={pixelated ? '[image-rendering:pixelated]' : undefined}
    />
  )
}
