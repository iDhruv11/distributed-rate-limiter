import * as React from 'react'
import { cn } from '../../lib/utils'

export const Input = React.forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...props }, ref) => (
    <input
      className={cn(
        'flex h-10 w-full rounded-xl border border-neutral-300 bg-white px-3 py-2 text-sm outline-none focus:border-black',
        className
      )}
      ref={ref}
      {...props}
    />
  )
)
Input.displayName = 'Input'
