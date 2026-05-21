# bg

The `bg` command is used to set a custom wallpaper or fetch random ones from the internet.

## Syntax
```
bg | bg random [-id=53] [-grayscale] [-blur=2]
```

## Usage
- `bg`: Opens the gallery to pick a custom image for the wallpaper.
- `bg -1`: Removes the custom wallpaper and reverts to the solid theme background.
- `bg random`: Fetches a random wallpaper from [picsum.photos](https://picsum.photos/images).
- `bg random -id=53`: Fetches a specific image by its ID.
- `bg random -grayscale`: Fetches a grayscale random image.
- `bg random -blur=5`: Fetches a blurred random image (blur intensity from 1 to 10).

!!! note
    This command only changes the Home screen wallpaper, not the Lock screen one.
