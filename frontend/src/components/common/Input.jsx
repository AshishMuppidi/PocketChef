export default function Input({
  label,
  id,
  type = 'text',
  placeholder = '',
  value,
  onChange,
  error,
  icon: Icon,
  className = '',
  required = false,
  ...props
}) {
  return (
    <div className="w-full">
      {label && (
        <label htmlFor={id} className="block text-sm font-medium text-[#111111] mb-1.5">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      <div className="relative rounded-lg shadow-xs">
        {Icon && (
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-[#555555]">
            <Icon className="h-5 w-5" />
          </div>
        )}
        <input
          id={id}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          className={`block w-full rounded-lg border border-[#D5D8D5] bg-white py-2.5 ${
            Icon ? 'pl-11' : 'pl-3.5'
          } pr-3.5 text-[#111111] placeholder-[#888888] focus:border-[#5AAE62] focus:ring-2 focus:ring-[#5AAE62]/20 focus:outline-none transition-colors sm:text-sm ${
            error ? 'border-red-500 focus:border-red-500 focus:ring-red-200' : ''
          } ${className}`}
          {...props}
        />
      </div>
      {error && <p className="mt-1.5 text-xs text-red-600">{error}</p>}
    </div>
  );
}
