require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "@aerogear/aerogear-reactnative-push"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  @aerogear/aerogear-reactnative-push
                   DESC
  s.homepage     = "https://github.com/aerogear/@aerogear/aerogear-reactnative-push"
  # brief license entry:
  s.license      = "APL"
  # optional - use expanded license entry instead:
  # s.license    = { :type => "APL", :file => "LICENSE" }
  s.authors      = { "Red Hat" => "aerogear@googlegroups.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/aerogear/@aerogear/aerogear-reactnative-push.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  # ...
  # s.dependency "..."
end

