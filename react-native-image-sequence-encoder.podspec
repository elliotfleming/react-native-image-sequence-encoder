require_relative 'package.json'
package = JSON.parse(File.read('package.json'))
folly_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32'

Pod::Spec.new do |s|
  s.name     = 'react-native-image-sequence-encoder'
  s.version  = package["version"]
  s.summary  = package["description"]
  s.homepage = package["homepage"]
  s.license  = package["license"]
  s.authors  = package["author"]

  s.platforms    = { :ios => '12.0' }
  s.swift_version = '5.0'

  s.source       = { :path => '.' }
  s.source_files = 'ios/**/*.{swift,h}'

  if respond_to?(:install_modules_dependencies, true)
    # RN ≥ 0.71 helper registers React-Core and, when enabled, Turbo/Fabric deps
    install_modules_dependencies(s)
  else
    s.dependency 'React-Core'
  end
spec
  # Extra compiler flags only when the consuming app turns on the new arch
  if ENV['RCT_NEW_ARCH_ENABLED'] == '1'
    s.compiler_flags = folly_flags + ' -DRCT_NEW_ARCH_ENABLED=1'
    s.pod_target_xcconfig = {
      'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/boost"',
      'OTHER_CPLUSPLUSFLAGS' => folly_flags,
      'CLANG_CXX_LANGUAGE_STANDARD' => 'c++17'
    }
  end
end
