import CallKit

extension CXProvider {
    // To ensure initializing only at once. Lazy stored property doesn't ensure it.
    static var custom: CXProvider {

        // Configure provider with sendbird's customzied configuration.
        let configuration = CXProviderConfiguration.custom
        let provider = CXProvider(configuration: configuration)

        return provider
    }
}